package com.example.proyect_plataformas.ui.screens.orders

import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceRequest

// Regla de orden de "Mis pedidos", sin Compose para poder probarla con JUnit.

/**
 * Primero lo que pide una acción del vecino (calificar), luego lo que está
 * en curso y al final lo ya calificado. Dentro de cada grupo, por fecha.
 */
fun ordersForDisplay(requests: List<ServiceRequest>): List<ServiceRequest> {
    return requests.sortedWith(
        compareBy<ServiceRequest> { request -> statusPriority(request.status) }
            .thenBy { request -> request.date }
    )
}

private fun statusPriority(status: RequestStatus): Int {
    return when (status) {
        RequestStatus.TO_RATE -> 0
        RequestStatus.SENT -> 1
        RequestStatus.RATED -> 2
    }
}
