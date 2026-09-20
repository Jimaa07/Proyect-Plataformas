package com.example.proyect_plataformas.data.mock

import com.example.proyect_plataformas.data.model.CompletedService
import com.example.proyect_plataformas.data.model.Provider

object MockLocalHandsData {

    val providers: List<Provider> = listOf(
        Provider(
            id = "carlos-mendez",
            name = "Carlos Méndez",
            specialty = "Electricidad residencial",
            zone = "Zona 10",
            experienceYears = 8,
            rating = 4.9,
            reviewCount = 142,
            isAvailable = true
        ),
        Provider(
            id = "roberto-garcia",
            name = "Roberto García",
            specialty = "Instalaciones comerciales",
            zone = "Mixco",
            experienceYears = 12,
            rating = 4.8,
            reviewCount = 89,
            isAvailable = false
        ),
        Provider(
            id = "ana-lopez",
            name = "Ana López",
            specialty = "Mantenimiento general",
            zone = "Zona 15",
            experienceYears = 5,
            rating = 5.0,
            reviewCount = 56,
            isAvailable = true
        )
    )

    val completedServices: List<CompletedService> = listOf(
        CompletedService(
            id = "mock-service-1",
            providerId = "carlos-mendez",
            providerName = "Carlos Méndez",
            serviceDescription = "Reparación de tablero eléctrico",
            completedDate = "12 de octubre de 2025"
        )
    )

    fun getProviderById(providerId: String): Provider? {
        return providers.firstOrNull { provider ->
            provider.id == providerId
        }
    }

    fun getCompletedServiceById(serviceId: String): CompletedService? {
        return completedServices.firstOrNull { service ->
            service.id == serviceId
        }
    }
}