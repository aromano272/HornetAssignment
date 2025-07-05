package com.hornet.movies.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.hornet.movies.presentation.list.ListIntent
import com.hornet.movies.presentation.list.ListNavigation
import com.hornet.movies.presentation.list.ListViewModel
import com.hornet.movies.ui.core.Screen
import com.hornet.movies.ui.list.ListScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

object Nav {

    @Serializable
    object List

}

fun NavGraphBuilder.graph(mainNavController: NavController) {

    composable<Nav.List> {
        val viewModel = koinViewModel<ListViewModel>()
        Screen(
            navController = mainNavController,
            viewModel = viewModel,
            navigationHandler = { event ->
                when (event) {
                    else -> throw UnsupportedOperationException()
                }
            },
        ) { state, onIntent ->
            ListScreen(
                state = state,
                onIntent = onIntent,
            )
        }
    }

}
