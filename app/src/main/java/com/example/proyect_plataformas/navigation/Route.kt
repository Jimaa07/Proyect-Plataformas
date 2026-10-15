package com.example.proyect_plataformas.navigation

import com.example.proyect_plataformas.ui.components.BottomTab

sealed class Route(val route: String) {

    data object Home : Route("home")

    // La categoría elegida no viaja en la ruta: la guarda el ViewModel
    // (ResultsState) con openResults() antes de navegar.
    data object Results : Route("results")

    data object Profile : Route("profile/{providerId}") {
        const val ARG_PROVIDER_ID = "providerId"
        fun create(providerId: String) = "profile/$providerId"
    }

    data object RequestService : Route("request/{providerId}") {
        const val ARG_PROVIDER_ID = "providerId"
        fun create(providerId: String) = "request/$providerId"
    }

    data object RateService : Route("rate/{requestId}") {
        const val ARG_REQUEST_ID = "requestId"
        fun create(requestId: String) = "rate/$requestId"
    }

    data object Orders : Route("orders")

    data object Account : Route("account")

    companion object {
        /** Ruta a la que lleva cada pestaña de la barra inferior. */
        fun forTab(tab: BottomTab): String {
            return when (tab) {
                BottomTab.HOME -> Home.route
                BottomTab.EXPLORE -> Results.route
                BottomTab.ORDERS -> Orders.route
                BottomTab.PROFILE -> Account.route
            }
        }
    }
}
