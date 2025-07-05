package com.hornet.movies.presentation.core

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class CommonViewState(
    val topBarViewState: TopBarViewState? = null,
    val errorAlert: String? = null,
    val successAlert: String? = null,
)

interface ViewStateWithCommonState : ViewState {
    val commonState: CommonViewState
}

data class CommonModelState(
    val errorAlert: String? = null,
    val successAlert: String? = null,
) {
    fun toViewState(
        topBarViewState: TopBarViewState? = null,
    ): CommonViewState = CommonViewState(
        topBarViewState = topBarViewState,
        errorAlert = errorAlert,
        successAlert = successAlert,
    )
}

data class TopBarViewState(
    val title: String,
    val backEnabled: Boolean = true,
    val onBackHandler: (() -> Unit)? = null,
    val actions: List<Action> = emptyList(),
) {
    data class Action(
        val icon: ImageVector,
        val onClick: () -> Unit,
    )
}

interface ModelStateWithCommonState<T : ModelStateWithCommonState<T>> : ModelState {
    val commonState: CommonModelState

    fun copyCommon(commonState: CommonModelState): T
}

interface ViewModel<
        TArgs : Args,
        TIntent : Intent,
        TModelState : ModelState,
        TViewState : ViewState,
        TNavigation : Navigation,
        > {
    val modelState: TModelState

    val viewStateFlow: StateFlow<TViewState>

    val navigationFlow: Flow<NavigationEvent<TNavigation>>

    fun onStart()
    fun onStop()

    fun mapViewState(state: TModelState): TViewState
    suspend fun handleIntent(state: TModelState, intent: TIntent)

    fun updateState(stateUpdate: (TModelState) -> TModelState)

    fun navigate(event: TNavigation)

    fun onIntent(intent: TIntent)

    fun launch(func: suspend CoroutineScope.(currentState: TModelState) -> Unit)
    fun launchJob(func: suspend CoroutineScope.(currentState: TModelState) -> Unit): Job

    fun launchOnStarted(func: suspend CoroutineScope.(currentState: TModelState) -> Unit)
    fun launchOnStartedJob(func: suspend CoroutineScope.(currentState: TModelState) -> Unit): Job
}

abstract class BaseViewModel<
        TArgs : Args,
        TIntent : Intent,
        TModelState : ModelState,
        TViewState : ViewState,
        TNavigation : Navigation,
        >(
    initialModelState: TModelState,
) : ViewModel<
        TArgs,
        TIntent,
        TModelState,
        TViewState,
        TNavigation
        >, androidx.lifecycle.ViewModel(), KoinComponent {

    private val modelStateFlow = MutableStateFlow(initialModelState)
    final override val modelState: TModelState
        get() = modelStateFlow.value

    private val _viewStateFlow by lazy { MutableStateFlow(mapViewState(initialModelState)) }
    final override val viewStateFlow: StateFlow<TViewState> by lazy {
        modelStateFlow.map { mapViewState(it) }
            .stateIn(viewModelScope, SharingStarted.Lazily, mapViewState(initialModelState))
    }

    private val _navigationFlow = MutableSharedFlow<NavigationEvent<TNavigation>>(replay = 10)

    protected val startedCoroutineScope =
        CoroutineScope(viewModelScope.coroutineContext + SupervisorJob())

    override fun onStart() {
        println("${this::class.simpleName} onStart")
    }

    override fun onStop() {
        startedCoroutineScope.coroutineContext.cancelChildren()
        println("${this::class.simpleName} onStop")
    }

    final override val navigationFlow: Flow<NavigationEvent<TNavigation>> = _navigationFlow
        .filterNot { it.wasHandled }

    final override fun updateState(stateUpdate: (TModelState) -> TModelState) {
        val oldState = modelStateFlow.value
        val newState = stateUpdate(oldState)

        println("ModelState: $newState")

        modelStateFlow.value = newState
        val newViewState = mapViewState(newState)
        println("ViewState: $newViewState")
        _viewStateFlow.value = newViewState
    }

    final override fun onIntent(intent: TIntent) = launch {
        println("onIntent: $intent")
        handleIntent(modelState, intent)
    }

    final override fun navigate(event: TNavigation) = launch {
        _navigationFlow.emit(NavigationEvent(event))
    }

    final override fun launch(func: suspend CoroutineScope.(currentState: TModelState) -> Unit) {
        viewModelScope.launch { func(modelState) }
    }

    final override fun launchJob(
        func: suspend CoroutineScope.(currentState: TModelState) -> Unit,
    ): Job = viewModelScope.launch { func(modelState) }

    final override fun launchOnStarted(func: suspend CoroutineScope.(currentState: TModelState) -> Unit) {
        startedCoroutineScope.launch { func(modelState) }
    }

    final override fun launchOnStartedJob(
        func: suspend CoroutineScope.(currentState: TModelState) -> Unit,
    ): Job = startedCoroutineScope.launch { func(modelState) }

    private var successAlertJob: Job? = null
    private var errorAlertJob: Job? = null

    fun showSuccessAlert(success: String) {
        if (modelState !is ModelStateWithCommonState<*>) return

        successAlertJob?.cancel()

        val job = launchJob {
            updateState {
                it as ModelStateWithCommonState<*>
                it.copyCommon(commonState = it.commonState.copy(successAlert = success)) as TModelState
            }
            delay(ALERT_DISPLAY_TIME)
        }.also {
            successAlertJob = it
        }
        job.invokeOnCompletion {
            if (job != successAlertJob) return@invokeOnCompletion

            updateState {
                it as ModelStateWithCommonState<*>
                it.copyCommon(commonState = it.commonState.copy(successAlert = null)) as TModelState
            }
        }
    }

    fun showErrorAlert(message: String) {
        val state = (modelState as? ModelStateWithCommonState<*>) ?: return

        errorAlertJob?.cancel()

        val job = launchJob {
            updateState {
                it as ModelStateWithCommonState<*>
                it.copyCommon(commonState = it.commonState.copy(errorAlert = message)) as TModelState
            }
            delay(ALERT_DISPLAY_TIME)
        }.also {
            errorAlertJob = it
        }
        job.invokeOnCompletion {
            if (job != errorAlertJob) return@invokeOnCompletion

            updateState {
                it as ModelStateWithCommonState<*>
                it.copyCommon(commonState = it.commonState.copy(errorAlert = null)) as TModelState
            }
        }
    }

    companion object {
        const val ALERT_DISPLAY_TIME = 4_000L
    }
}

interface Args
interface Intent
interface ModelState

@Stable
interface ViewState
interface Navigation

class NavigationEvent<TNavigation : Navigation>(
    val navigation: TNavigation,
) {
    var wasHandled: Boolean = false
        private set

    fun markAsHandled() {
        wasHandled = true
    }
}
