package com.example.proyect_plataformas.ui.state

import com.example.proyect_plataformas.data.model.CompletedService
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.ProviderFilter
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.ui.format.formatLongDate
import com.example.proyect_plataformas.ui.screens.rating.isRatingFormValid
import java.time.LocalDate

/** Filtros que el vecino eligió en Resultados. */
data class ResultsState(
    val category: ServiceCategory? = null,
    val query: String = "",
    val filter: ProviderFilter = ProviderFilter.TOP_RATED
)

/** Formulario de solicitud mientras el vecino lo llena y lo envía. */
data class RequestFormState(
    val providerId: String,
    val description: String = "",
    val zone: String? = null,
    val date: LocalDate? = null,
    val timeSlot: TimeSlot? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitted: Boolean = false
)

/** Formulario para calificar un pedido terminado. */
data class RatingFormState(
    val requestId: String,
    val stars: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitted: Boolean = false
) {
    val isValid: Boolean
        get() = isRatingFormValid(stars, comment)
}

/** Perfil y Solicitar buscan al prestador del argumento dentro de la lista observada. */
fun List<Provider>.findProvider(providerId: String): Provider? {
    return firstOrNull { provider -> provider.id == providerId }
}

/** Zonas donde trabaja; si no tiene lista, su zona principal. */
fun coverageZonesOf(provider: Provider): List<String> {
    return provider.coverageZones.ifEmpty { listOf(provider.zone) }
}

/** RateServiceScreen trabaja con CompletedService; aquí se adapta el pedido. */
fun ServiceRequest.toCompletedService(): CompletedService {
    return CompletedService(
        id = id,
        providerId = providerId,
        providerName = providerName,
        serviceDescription = description,
        completedDate = formatLongDate(date)
    )
}
