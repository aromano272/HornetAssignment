package com.hornet.movies.ui.core

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import com.hornet.movies.presentation.core.Intent
import com.hornet.movies.presentation.core.Navigation
import com.hornet.movies.presentation.core.ViewModel
import com.hornet.movies.presentation.core.ViewState
import com.hornet.movies.presentation.core.ViewStateWithCommonState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <
        TIntent : Intent,
        TViewState : ViewState,
        TNavigation : Navigation,
        VM : ViewModel<*, TIntent, *, TViewState, TNavigation>,
        > Screen(
    navController: NavController,
    viewModel: VM,
    navigationHandler: NavController.(TNavigation) -> Unit,
    content: @Composable (state: TViewState, onIntent: (TIntent) -> Unit) -> Unit,
) {
    val currentOnStart by rememberUpdatedState(viewModel::onStart)
    val currentOnStop by rememberUpdatedState(viewModel::onStop)

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        currentOnStart()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        currentOnStop()
    }

    val viewState by viewModel.viewStateFlow.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.navigationFlow
            .onEach { event ->
                event.markAsHandled()
                navController.navigationHandler(event.navigation)
            }.collect()
    }


    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            (viewState as? ViewStateWithCommonState)?.commonState?.let { state ->
                state.topBarViewState?.let { topBar ->
                    BackHandler(enabled = topBar.onBackHandler != null) {
                        topBar.onBackHandler?.invoke()
                    }

                    TopAppBar(
                        title = { Text(topBar.title) },
                        navigationIcon = {
                            if (topBar.backEnabled) {
                                IconButton(onClick = {
                                    topBar.onBackHandler?.invoke() ?: navController.navigateUp()
                                }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        actions = {
                            topBar.actions.forEach { action ->
                                IconButton(onClick = action.onClick) {
                                    Icon(action.icon, contentDescription = null)
                                }
                            }
                        }
                    )
                }
            }

            content(viewState, viewModel::onIntent)
        }

        (viewState as? ViewStateWithCommonState)?.commonState?.let { state ->
            (state.errorAlert ?: state.successAlert)?.let {
                LocalSoftwareKeyboardController.current?.hide()
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    snackbarData = CustomSnackbarData(it),
                )
            }
        }
    }
}

data class CustomSnackbarData(
    override val message: String,
) : SnackbarData, SnackbarVisuals {
    override val duration: SnackbarDuration = SnackbarDuration.Indefinite

    override val visuals: SnackbarVisuals = this

    override fun dismiss() {
    }

    override fun performAction() {
    }

    override val actionLabel: String? = null
    override val withDismissAction: Boolean = false
}