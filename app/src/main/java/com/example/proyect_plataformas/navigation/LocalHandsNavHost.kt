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
import com.example.proyect_plataformas.ui.screens.HomeScreen

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
            PlaceholderScreen("Resultados de electricistas")
        }

        composable(
            route = Route.Profile.route,
            arguments = listOf(navArgument("providerId") { defaultValue = "" })
        ) {
            PlaceholderScreen("Perfil del proveedor")
        }

        composable(
            route = Route.RequestService.route,
            arguments = listOf(navArgument("providerId") { defaultValue = "" })
        ) {
            PlaceholderScreen("Solicitar servicio")
        }

        composable(
            route = Route.RateService.route,
            arguments = listOf(navArgument("serviceId") { defaultValue = "" })
        ) {
            PlaceholderScreen("Calificar servicio")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
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