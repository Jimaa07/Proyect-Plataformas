package com.example.proyect_plataformas.navigation

import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.ui.components.BottomTab

sealed class Route(val route: String) {

    data object Home : Route("home")

    // La categoría viaja como argumento opcional: sin él se muestran todas.
    data object Results : Route("results?category={category}") {
        const val ARG_CATEGORY = "category"
        fun create(category: ServiceCategory? = null): String {
            return if (category == null) "results" else "results?category=${category.name}"
        }
    }

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
                BottomTab.EXPLORE -> Results.create()
                BottomTab.ORDERS -> Orders.route
                BottomTab.PROFILE -> Account.route
            }
        }
    }
}
