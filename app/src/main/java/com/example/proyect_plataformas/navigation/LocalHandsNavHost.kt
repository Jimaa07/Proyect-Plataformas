package com.example.proyect_plataformas.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.screens.HomeScreen
import com.example.proyect_plataformas.ui.screens.account.AccountScreen
import com.example.proyect_plataformas.ui.screens.orders.OrdersScreen
import com.example.proyect_plataformas.ui.screens.profile.ProfileScreen
import com.example.proyect_plataformas.ui.screens.rating.RateServiceScreen
import com.example.proyect_plataformas.ui.screens.request.RequestServiceScreen
import com.example.proyect_plataformas.ui.screens.results.ResultsScreen
import com.example.proyect_plataformas.ui.state.LocalHandsViewModel
import com.example.proyect_plataformas.ui.state.toCompletedService

/**
 * Conecta el ViewModel con las pantallas (MVVM, flujo unidireccional):
 * el estado baja desde viewModel.uiState y los eventos suben como llamadas
 * a funciones del ViewModel. Las pantallas no conocen al ViewModel ni al
 * navController; solo reciben valores y callbacks.
 */
@Composable
fun LocalHandsNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: LocalHandsViewModel = viewModel()
) {
    // Se deja de observar cuando la app pasa a segundo plano.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    fun openResults(category: ServiceCategory?) {
        viewModel.openResults(category)
        navController.navigate(Route.Results.route) {
            launchSingleTop = true
        }
    }

    // Las pestañas no apilan pantallas: siempre quedan encima de Inicio.
    fun goToTab(tab: BottomTab) {
        when (tab) {
            BottomTab.HOME -> {
                navController.popBackStack(Route.Home.route, inclusive = false)
            }

            else -> {
                if (tab == BottomTab.EXPLORE) {
                    viewModel.openResults(category = null)
                }

                navController.navigate(Route.forTab(tab)) {
                    popUpTo(Route.Home.route)
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {

        composable(Route.Home.route) {
            HomeScreen(
                userName = uiState.currentUser.name,
                recommendedProviders = uiState.recommendedProviders(limit = 3),
                pendingRatings = uiState.requestsToRate.size,
                onSearchClick = {
                    openResults(category = null)
                },
                onCategoryClick = { category ->
                    openResults(category)
                },
                onProviderClick = { providerId ->
                    navController.navigate(Route.Profile.create(providerId))
                },
                onPendingRatingsClick = {
                    goToTab(BottomTab.ORDERS)
                },
                onTabSelected = ::goToTab
            )
        }

        composable(Route.Results.route) {
            ResultsScreen(
                state = uiState.results,
                providers = uiState.visibleProviders,
                onBackClick = {
                    navController.popBackStack()
                },
                onProviderClick = { providerId ->
                    navController.navigate(Route.Profile.create(providerId))
                },
                onCategorySelected = viewModel::selectCategory,
                onQueryChanged = viewModel::updateQuery,
                onFilterSelected = viewModel::selectFilter,
                onClearFilters = viewModel::clearResultsFilters,
                onTabSelected = ::goToTab
            )
        }

        composable(
            route = Route.Profile.route,
            arguments = listOf(
                navArgument(Route.Profile.ARG_PROVIDER_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments
                ?.getString(Route.Profile.ARG_PROVIDER_ID)
                .orEmpty()

            val provider = uiState.providerById(providerId)

            if (provider != null) {
                ProfileScreen(
                    provider = provider,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestServiceClick = {
                        viewModel.startRequest(provider.id)
                        navController.navigate(Route.RequestService.create(provider.id))
                    }
                )
            } else {
                NotFoundScreen(
                    title = "Prestador no encontrado",
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Route.RequestService.route,
            arguments = listOf(
                navArgument(Route.RequestService.ARG_PROVIDER_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val providerId = backStackEntry.arguments
                ?.getString(Route.RequestService.ARG_PROVIDER_ID)
                .orEmpty()

            val provider = uiState.providerById(providerId)

            if (provider != null) {
                RequestServiceScreen(
                    provider = provider,
                    form = uiState.requestFormFor(provider.id),
                    formErrors = uiState.requestErrorsFor(provider.id),
                    today = uiState.today,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onDescriptionChanged = { text ->
                        viewModel.updateDescription(provider.id, text)
                    },
                    onZoneSelected = { zone ->
                        viewModel.selectZone(provider.id, zone)
                    },
                    onDateSelected = { date ->
                        viewModel.selectDate(provider.id, date)
                    },
                    onTimeSlotSelected = { slot ->
                        viewModel.selectTimeSlot(provider.id, slot)
                    },
                    onSubmitClick = {
                        viewModel.submitRequest(provider.id)
                    },
                    onFinished = {
                        // Después de enviar se muestra el pedido en "Mis pedidos".
                        goToTab(BottomTab.ORDERS)
                    }
                )
            } else {
                NotFoundScreen(
                    title = "Prestador no encontrado",
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = Route.RateService.route,
            arguments = listOf(
                navArgument(Route.RateService.ARG_REQUEST_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments
                ?.getString(Route.RateService.ARG_REQUEST_ID)
                .orEmpty()

            val request = uiState.requestById(requestId)

            if (request != null) {
                RateServiceScreen(
                    service = request.toCompletedService(),
                    form = uiState.ratingFormFor(requestId),
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onStarsSelected = { stars ->
                        viewModel.selectStars(requestId, stars)
                    },
                    onCommentChanged = { comment ->
                        viewModel.updateComment(requestId, comment)
                    },
                    onSubmitClick = {
                        viewModel.submitRating(requestId)
                    },
                    onFinished = {
                        navController.popBackStack()
                    }
                )
            } else {
                NotFoundScreen(
                    title = "Servicio no encontrado",
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(Route.Orders.route) {
            OrdersScreen(
                requests = uiState.requests,
                onRequestClick = { providerId ->
                    navController.navigate(Route.Profile.create(providerId))
                },
                onRateClick = { requestId ->
                    viewModel.startRating(requestId)
                    navController.navigate(Route.RateService.create(requestId))
                },
                onExploreClick = {
                    goToTab(BottomTab.EXPLORE)
                },
                onTabSelected = ::goToTab
            )
        }

        composable(Route.Account.route) {
            AccountScreen(
                user = uiState.currentUser,
                activeRequests = uiState.requests.count { request ->
                    request.status == RequestStatus.SENT
                },
                pendingRatings = uiState.requestsToRate.size,
                reviewsWritten = uiState.requests.count { request ->
                    request.status == RequestStatus.RATED
                },
                onOrdersClick = {
                    goToTab(BottomTab.ORDERS)
                },
                onTabSelected = ::goToTab
            )
        }
    }
}

@Composable
private fun NotFoundScreen(
    title: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )

        TextButton(
            onClick = onBackClick
        ) {
            Text("Regresar")
        }
    }
}
