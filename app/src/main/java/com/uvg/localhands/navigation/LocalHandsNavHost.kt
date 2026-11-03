package com.uvg.localhands.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uvg.localhands.data.model.RequestStatus
import com.uvg.localhands.data.model.ServiceCategory
import com.uvg.localhands.ui.components.BottomTab
import com.uvg.localhands.ui.components.DetailStatusScreen
import com.uvg.localhands.ui.screens.HomeScreen
import com.uvg.localhands.ui.screens.HomeViewModel
import com.uvg.localhands.ui.screens.account.AccountScreen
import com.uvg.localhands.ui.screens.account.AccountViewModel
import com.uvg.localhands.ui.screens.orders.OrdersScreen
import com.uvg.localhands.ui.screens.orders.OrdersViewModel
import com.uvg.localhands.ui.screens.profile.ProfileScreen
import com.uvg.localhands.ui.screens.profile.ProfileViewModel
import com.uvg.localhands.ui.screens.rating.RateServiceScreen
import com.uvg.localhands.ui.screens.rating.RateServiceViewModel
import com.uvg.localhands.ui.screens.request.RequestServiceScreen
import com.uvg.localhands.ui.screens.request.RequestServiceViewModel
import com.uvg.localhands.ui.screens.results.ResultsScreen
import com.uvg.localhands.ui.screens.results.ResultsViewModel
import com.uvg.localhands.ui.state.DetailState
import com.uvg.localhands.ui.state.toCompletedService

/**
 * Conecta cada pantalla con su ViewModel (MVVM, flujo unidireccional).
 * Cada destino obtiene su propio ViewModel con su Factory; los argumentos
 * de la ruta le llegan por SavedStateHandle. Las pantallas no conocen al
 * ViewModel ni al navController: solo reciben valores y callbacks.
 */
@Composable
fun LocalHandsNavHost(
    navController: NavHostController = rememberNavController()
) {
    // Solo se navega desde la pantalla visible: evita abrir dos veces la misma
    // pantalla o regresar dos veces cuando se toca rápido durante una transición.
    fun NavBackStackEntry.navigateIfVisible(route: String) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            navController.navigate(route)
        }
    }

    fun NavBackStackEntry.goBackIfVisible() {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            navController.popBackStack()
        }
    }

    // Las pestañas no apilan pantallas: siempre quedan encima de Inicio.
    fun goToTab(tab: BottomTab) {
        when (tab) {
            BottomTab.HOME -> {
                navController.popBackStack(Route.Home.route, inclusive = false)
            }

            // Explorar siempre empieza sin filtros, aunque ya hubiera unos Resultados abiertos.
            BottomTab.EXPLORE -> {
                navController.navigate(Route.Results.create()) {
                    popUpTo(Route.Home.route)
                }
            }

            else -> {
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

        composable(Route.Home.route) { entry ->
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            fun openResults(category: ServiceCategory?) {
                entry.navigateIfVisible(Route.Results.create(category))
            }

            HomeScreen(
                userName = uiState.userName,
                recommendedProviders = uiState.recommendedProviders,
                pendingRatings = uiState.pendingRatings,
                onSearchClick = {
                    openResults(category = null)
                },
                onCategoryClick = { category ->
                    openResults(category)
                },
                onProviderClick = { providerId ->
                    entry.navigateIfVisible(Route.Profile.create(providerId))
                },
                onPendingRatingsClick = {
                    goToTab(BottomTab.ORDERS)
                },
                onTabSelected = ::goToTab,
                recommendedStatus = uiState.recommendedStatus,
                onRetryRecommended = viewModel::retry
            )
        }

        composable(
            route = Route.Results.route,
            arguments = listOf(
                navArgument(Route.Results.ARG_CATEGORY) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val viewModel: ResultsViewModel = viewModel(factory = ResultsViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            ResultsScreen(
                state = uiState.filters,
                providers = uiState.providers,
                onBackClick = {
                    entry.goBackIfVisible()
                },
                onProviderClick = { providerId ->
                    entry.navigateIfVisible(Route.Profile.create(providerId))
                },
                onCategorySelected = viewModel::selectCategory,
                onQueryChanged = viewModel::updateQuery,
                onFilterSelected = viewModel::selectFilter,
                onClearFilters = viewModel::clearFilters,
                onTabSelected = ::goToTab,
                status = uiState.status,
                onRetry = viewModel::retry
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
        ) { entry ->
            val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            when (val state = uiState) {
                is DetailState.Ready -> {
                    ProfileScreen(
                        provider = state.item,
                        onBackClick = {
                            entry.goBackIfVisible()
                        },
                        onRequestServiceClick = {
                            entry.navigateIfVisible(Route.RequestService.create(state.item.id))
                        },
                        notice = state.notice
                    )
                }

                else -> {
                    DetailStatusScreen(
                        title = "Perfil del profesional",
                        state = state,
                        notFoundMessage = "Este profesional ya no está disponible.",
                        onBackClick = {
                            entry.goBackIfVisible()
                        },
                        onRetry = viewModel::retry
                    )
                }
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
        ) { entry ->
            val viewModel: RequestServiceViewModel = viewModel(factory = RequestServiceViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            when (val providerState = uiState.providerState) {
                is DetailState.Ready -> {
                    RequestServiceScreen(
                        provider = providerState.item,
                        form = uiState.form,
                        formErrors = uiState.formErrors,
                        today = uiState.today,
                        onBackClick = {
                            entry.goBackIfVisible()
                        },
                        onDescriptionChanged = viewModel::updateDescription,
                        onZoneSelected = viewModel::selectZone,
                        onDateSelected = viewModel::selectDate,
                        onTimeSlotSelected = viewModel::selectTimeSlot,
                        onSaveDraftClick = viewModel::saveDraft,
                        onSubmitClick = viewModel::submit,
                        notice = providerState.notice,
                        onFinished = {
                            // Después de enviar se muestra la solicitud en "Mis pedidos".
                            goToTab(BottomTab.ORDERS)
                        }
                    )
                }

                else -> {
                    DetailStatusScreen(
                        title = "Solicitar servicio",
                        state = providerState,
                        notFoundMessage = "Este profesional ya no está disponible.",
                        onBackClick = {
                            entry.goBackIfVisible()
                        },
                        onRetry = viewModel::retry
                    )
                }
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
        ) { entry ->
            val viewModel: RateServiceViewModel = viewModel(factory = RateServiceViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val requestState = uiState.requestState

            // Solo se califica un servicio pendiente; el que se está calificando
            // o se acaba de calificar aquí sigue visible hasta la confirmación.
            val form = uiState.form
            val canRate = requestState is DetailState.Ready &&
                    (requestState.item.status == RequestStatus.TO_RATE || form.isSubmitting || form.submitted)

            if (requestState is DetailState.Ready && canRate) {
                RateServiceScreen(
                    service = requestState.item.toCompletedService(),
                    form = uiState.form,
                    onBackClick = {
                        entry.goBackIfVisible()
                    },
                    onStarsSelected = viewModel::selectStars,
                    onCommentChanged = viewModel::updateComment,
                    onSubmitClick = viewModel::submit,
                    onFinished = {
                        entry.goBackIfVisible()
                    }
                )
            } else {
                DetailStatusScreen(
                    title = "Calificar servicio",
                    state = if (requestState is DetailState.Ready) DetailState.NotFound else requestState,
                    notFoundMessage = if (requestState is DetailState.Ready) {
                        "Este servicio ya fue calificado."
                    } else {
                        "Este servicio ya no existe."
                    },
                    onBackClick = {
                        entry.goBackIfVisible()
                    },
                    onRetry = viewModel::retry
                )
            }
        }

        composable(Route.Orders.route) { entry ->
            val viewModel: OrdersViewModel = viewModel(factory = OrdersViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            OrdersScreen(
                requests = uiState.requests,
                onRequestClick = { providerId ->
                    entry.navigateIfVisible(Route.Profile.create(providerId))
                },
                onRateClick = { requestId ->
                    entry.navigateIfVisible(Route.RateService.create(requestId))
                },
                onExploreClick = {
                    goToTab(BottomTab.EXPLORE)
                },
                onTabSelected = ::goToTab,
                status = uiState.status,
                onRetry = viewModel::retry
            )
        }

        composable(Route.Account.route) {
            val viewModel: AccountViewModel = viewModel(factory = AccountViewModel.Factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AccountScreen(
                state = uiState,
                onEditProfile = viewModel::startEditing,
                onCancelEdit = viewModel::cancelEditing,
                onNameChanged = viewModel::updateName,
                onZoneChanged = viewModel::updateZone,
                onPhoneChanged = viewModel::updatePhone,
                onEmailChanged = viewModel::updateEmail,
                onSaveProfile = viewModel::saveProfile,
                onRetry = viewModel::retry,
                onOrdersClick = {
                    goToTab(BottomTab.ORDERS)
                },
                onTabSelected = ::goToTab
            )
        }
    }
}
