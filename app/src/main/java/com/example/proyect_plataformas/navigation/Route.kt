package com.example.proyect_plataformas.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Results : Route("results")
    data object Profile : Route("profile/{providerId}") {
        fun create(providerId: String) = "profile/$providerId"
    }
    data object RequestService : Route("request/{providerId}") {
        fun create(providerId: String) = "request/$providerId"
    }
    data object RateService : Route("rate/{serviceId}") {
        fun create(serviceId: String) = "rate/$serviceId"
    }
}