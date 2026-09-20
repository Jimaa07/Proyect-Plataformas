package com.example.proyect_plataformas.data.mock

import com.example.proyect_plataformas.data.model.CommunityReview
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
            isAvailable = true,
            description = "Electricista especializado en instalaciones residenciales, " +
                    "reparación de tableros eléctricos, iluminación y mantenimiento preventivo. " +
                    "Trabajo con puntualidad y explico cada reparación antes de realizarla.",
            coverageZones = listOf(
                "Zona 10",
                "Zona 14",
                "Zona 15",
                "Zona 16"
            ),
            contactPhone = "+502 5555-0101",
            contactEmail = "carlos@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "María López",
                    rating = 5,
                    comment = "Llegó puntual y resolvió el problema rápidamente. Muy recomendado.",
                    date = "10 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Luis Herrera",
                    rating = 5,
                    comment = "Explicó claramente qué estaba fallando y dejó todo funcionando bien.",
                    date = "2 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Andrea Castillo",
                    rating = 4,
                    comment = "Buen servicio y buena comunicación durante todo el trabajo.",
                    date = "25 de agosto de 2026"
                )
            )
        ),

        Provider(
            id = "roberto-garcia",
            name = "Roberto García",
            specialty = "Instalaciones comerciales",
            zone = "Mixco",
            experienceYears = 12,
            rating = 4.8,
            reviewCount = 89,
            isAvailable = false,
            description = "Técnico electricista con experiencia en instalaciones comerciales, " +
                    "mantenimiento de circuitos y diagnóstico de fallas eléctricas.",
            coverageZones = listOf(
                "Mixco",
                "Zona 7",
                "Zona 11"
            ),
            contactPhone = "+502 5555-0102",
            contactEmail = "roberto@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Sofía Ramírez",
                    rating = 5,
                    comment = "Muy profesional y ordenado durante toda la instalación.",
                    date = "6 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Daniel Morales",
                    rating = 4,
                    comment = "Buen trabajo y atención. Volvería a contratarlo.",
                    date = "18 de agosto de 2026"
                )
            )
        ),

        Provider(
            id = "ana-lopez",
            name = "Ana López",
            specialty = "Mantenimiento general",
            zone = "Zona 15",
            experienceYears = 5,
            rating = 5.0,
            reviewCount = 56,
            isAvailable = true,
            description = "Profesional de mantenimiento residencial con experiencia en " +
                    "reparaciones eléctricas básicas, instalación de luminarias y mantenimiento general.",
            coverageZones = listOf(
                "Zona 10",
                "Zona 15",
                "Zona 16",
                "Carretera a El Salvador"
            ),
            contactPhone = "+502 5555-0103",
            contactEmail = "ana@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "José Pérez",
                    rating = 5,
                    comment = "Excelente atención. Terminó el trabajo antes de lo esperado.",
                    date = "12 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Gabriela Ruiz",
                    rating = 5,
                    comment = "Muy amable, puntual y cuidadosa con el trabajo.",
                    date = "30 de agosto de 2026"
                )
            )
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