package com.example.proyect_plataformas.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.ui.screens.HomeScreen
import com.example.proyect_plataformas.ui.screens.profile.ProfileScreen
import com.example.proyect_plataformas.ui.screens.rating.RateServiceScreen
import com.example.proyect_plataformas.ui.screens.request.RequestServiceScreen
import com.example.proyect_plataformas.ui.screens.results.ResultsScreen

@Composable
fun LocalHandsNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {

        composable(Route.Home.route) {
            HomeScreen(
                onCategoryClick = {
                    navController.navigate(Route.Results.route)
                },
                onRateServiceClick = {
                    navController.navigate(
                        Route.RateService.create("mock-service-1")
                    )
                }
            )
        }

        composable(Route.Results.route) {
            ResultsScreen(
                providers = MockLocalHandsData.providers,
                onBackClick = {
                    navController.popBackStack()
                },
                onProviderClick = { providerId ->
                    navController.navigate(
                        Route.Profile.create(providerId)
                    )
                }
            )
        }

        composable(
            route = Route.Profile.route,
            arguments = listOf(
                navArgument("providerId") {
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val providerId =
                backStackEntry.arguments
                    ?.getString("providerId")
                    .orEmpty()

            val provider =
                MockLocalHandsData.getProviderById(
                    providerId = providerId
                )

            if (provider != null) {
                ProfileScreen(
                    provider = provider,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestServiceClick = {
                        navController.navigate(
                            Route.RequestService.create(
                                provider.id
                            )
                        )
                    }
                )
            } else {
                PlaceholderScreen(
                    title = "Prestador no encontrado"
                )
            }
        }

        composable(
            route = Route.RequestService.route,
            arguments = listOf(
                navArgument("providerId") {
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val providerId =
                backStackEntry.arguments
                    ?.getString("providerId")
                    .orEmpty()

            val provider =
                MockLocalHandsData.getProviderById(
                    providerId = providerId
                )

            if (provider != null) {
                RequestServiceScreen(
                    provider = provider,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRequestSubmitted = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen(
                    title = "Prestador no encontrado"
                )
            }
        }

        composable(
            route = Route.RateService.route,
            arguments = listOf(
                navArgument("serviceId") {
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->

            val serviceId =
                backStackEntry.arguments
                    ?.getString("serviceId")
                    .orEmpty()

            val completedService =
                MockLocalHandsData.getCompletedServiceById(
                    serviceId = serviceId
                )

            if (completedService != null) {
                RateServiceScreen(
                    service = completedService,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onRatingSubmitted = {
                        navController.popBackStack()
                    }
                )
            } else {
                PlaceholderScreen(
                    title = "Servicio no encontrado"
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}