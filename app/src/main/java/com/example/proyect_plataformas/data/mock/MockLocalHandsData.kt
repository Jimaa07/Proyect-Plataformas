package com.example.proyect_plataformas.data.mock

import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceCategory
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.data.model.UserProfile
import java.time.LocalDate

object MockLocalHandsData {

    val currentUser = UserProfile(
        name = "Valeria Soto",
        zone = "Zona 10",
        phone = "+502 5555-0199",
        email = "valeria.soto@correo.com"
    )

    // Cada categoría del inicio tiene al menos dos prestadores para que
    // el filtrado por categoría muestre resultados distintos.
    // A cada reseña se le asigna un id estable para usarlo como clave en las listas.
    val providers: List<Provider> by lazy {
        baseProviders.map { provider ->
            provider.copy(
                reviews = provider.reviews.mapIndexed { index, review ->
                    review.copy(id = "${provider.id}-review-${index + 1}")
                }
            )
        }
    }

    private val baseProviders: List<Provider> = listOf(

        // ---------- Electricidad ----------
        Provider(
            id = "carlos-mendez",
            name = "Carlos Méndez",
            specialty = "Electricidad residencial",
            category = ServiceCategory.ELECTRICAL,
            zone = "Zona 10",
            experienceYears = 8,
            rating = 4.9,
            reviewCount = 142,
            isAvailable = true,
            description = "Electricista especializado en instalaciones residenciales, " +
                    "reparación de tableros eléctricos, iluminación y mantenimiento preventivo. " +
                    "Trabajo con puntualidad y explico cada reparación antes de realizarla.",
            coverageZones = listOf("Zona 10", "Zona 14", "Zona 15", "Zona 16"),
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
            category = ServiceCategory.ELECTRICAL,
            zone = "Mixco",
            experienceYears = 12,
            rating = 4.8,
            reviewCount = 89,
            isAvailable = false,
            description = "Técnico electricista con experiencia en instalaciones comerciales, " +
                    "mantenimiento de circuitos y diagnóstico de fallas eléctricas.",
            coverageZones = listOf("Mixco", "Zona 7", "Zona 11"),
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
            category = ServiceCategory.ELECTRICAL,
            zone = "Zona 15",
            experienceYears = 5,
            rating = 5.0,
            reviewCount = 56,
            isAvailable = true,
            description = "Profesional de mantenimiento residencial con experiencia en " +
                    "reparaciones eléctricas básicas, instalación de luminarias y mantenimiento general.",
            coverageZones = listOf("Zona 10", "Zona 15", "Zona 16", "Carretera a El Salvador"),
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
        ),

        // ---------- Plomería ----------
        Provider(
            id = "carlos-hernandez",
            name = "Carlos Hernández",
            specialty = "Plomería residencial",
            category = ServiceCategory.PLUMBING,
            zone = "Zona 11",
            experienceYears = 10,
            rating = 4.9,
            reviewCount = 124,
            isAvailable = true,
            description = "Plomero con experiencia en fugas, cambio de tuberías, instalación " +
                    "de sanitarios y calentadores. Llevo los repuestos más comunes para " +
                    "resolver en la primera visita.",
            coverageZones = listOf("Zona 11", "Zona 12", "Mixco"),
            contactPhone = "+502 5555-0201",
            contactEmail = "carlos.h@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Patricia Gómez",
                    rating = 5,
                    comment = "Encontró la fuga del baño en minutos y dejó todo limpio.",
                    date = "28 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Héctor Juárez",
                    rating = 5,
                    comment = "Cobró lo que dijo desde el inicio. Muy honesto.",
                    date = "14 de septiembre de 2026"
                )
            )
        ),

        Provider(
            id = "luis-ramirez",
            name = "Luis Ramírez",
            specialty = "Destapes y fugas",
            category = ServiceCategory.PLUMBING,
            zone = "Villa Nueva",
            experienceYears = 6,
            rating = 4.6,
            reviewCount = 37,
            isAvailable = false,
            description = "Destape de drenajes, reparación de fugas y mantenimiento de " +
                    "cisternas y bombas de agua.",
            coverageZones = listOf("Villa Nueva", "Zona 12", "Zona 21"),
            contactPhone = "+502 5555-0202",
            contactEmail = "luis@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Karla Méndez",
                    rating = 4,
                    comment = "Resolvió el destape, aunque llegó un poco tarde.",
                    date = "20 de septiembre de 2026"
                )
            )
        ),

        // ---------- Carpintería ----------
        Provider(
            id = "maria-garcia",
            name = "María García",
            specialty = "Muebles a medida",
            category = ServiceCategory.CARPENTRY,
            zone = "Zona 15",
            experienceYears = 9,
            rating = 5.0,
            reviewCount = 42,
            isAvailable = true,
            description = "Diseño y fabricación de muebles a medida, closets y cocinas. " +
                    "Hago visita de medición sin costo dentro de mis zonas.",
            coverageZones = listOf("Zona 10", "Zona 15", "Zona 16"),
            contactPhone = "+502 5555-0301",
            contactEmail = "maria@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Fernando Ruiz",
                    rating = 5,
                    comment = "El closet quedó exactamente como lo pedimos.",
                    date = "22 de septiembre de 2026"
                ),
                CommunityReview(
                    reviewerName = "Lucía Morales",
                    rating = 5,
                    comment = "Muy detallista y cumplió con la fecha de entrega.",
                    date = "5 de septiembre de 2026"
                )
            )
        ),

        Provider(
            id = "jorge-castillo",
            name = "Jorge Castillo",
            specialty = "Puertas y reparaciones",
            category = ServiceCategory.CARPENTRY,
            zone = "Zona 1",
            experienceYears = 14,
            rating = 4.7,
            reviewCount = 63,
            isAvailable = true,
            description = "Reparación de puertas, chapas, gabinetes y muebles de madera. " +
                    "También barnizo y restauro muebles antiguos.",
            coverageZones = listOf("Zona 1", "Zona 2", "Zona 5", "Zona 6"),
            contactPhone = "+502 5555-0302",
            contactEmail = "jorge@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Ramiro Paz",
                    rating = 5,
                    comment = "Arregló la puerta principal que ya no cerraba. Excelente.",
                    date = "17 de septiembre de 2026"
                )
            )
        ),

        // ---------- Jardinería ----------
        Provider(
            id = "rosa-cifuentes",
            name = "Rosa Cifuentes",
            specialty = "Diseño y mantenimiento de jardines",
            category = ServiceCategory.GARDENING,
            zone = "Carretera a El Salvador",
            experienceYears = 7,
            rating = 4.8,
            reviewCount = 51,
            isAvailable = true,
            description = "Diseño de jardines, siembra, poda y mantenimiento mensual. " +
                    "Trabajo con plantas nativas que necesitan poca agua.",
            coverageZones = listOf("Carretera a El Salvador", "Zona 15", "Zona 16"),
            contactPhone = "+502 5555-0401",
            contactEmail = "rosa@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Mónica Herrera",
                    rating = 5,
                    comment = "El jardín cambió por completo. Muy creativa.",
                    date = "26 de septiembre de 2026"
                )
            )
        ),

        Provider(
            id = "pedro-ajcuc",
            name = "Pedro Ajcuc",
            specialty = "Poda y corte de grama",
            category = ServiceCategory.GARDENING,
            zone = "Mixco",
            experienceYears = 4,
            rating = 4.5,
            reviewCount = 22,
            isAvailable = false,
            description = "Corte de grama, poda de árboles pequeños y limpieza de áreas verdes.",
            coverageZones = listOf("Mixco", "Zona 7", "Zona 19"),
            contactPhone = "+502 5555-0402",
            contactEmail = "pedro@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Óscar López",
                    rating = 4,
                    comment = "Buen trabajo y precio justo.",
                    date = "9 de septiembre de 2026"
                )
            )
        ),

        // ---------- Electrodomésticos ----------
        Provider(
            id = "andres-morales",
            name = "Andrés Morales",
            specialty = "Refrigeradoras y lavadoras",
            category = ServiceCategory.APPLIANCES,
            zone = "Zona 7",
            experienceYears = 11,
            rating = 4.7,
            reviewCount = 78,
            isAvailable = true,
            description = "Diagnóstico y reparación de refrigeradoras, lavadoras y secadoras " +
                    "de todas las marcas. Doy garantía de 3 meses en repuestos.",
            coverageZones = listOf("Zona 7", "Zona 11", "Mixco", "Zona 10"),
            contactPhone = "+502 5555-0501",
            contactEmail = "andres@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Elena Pineda",
                    rating = 5,
                    comment = "La lavadora quedó como nueva y me explicó cómo cuidarla.",
                    date = "29 de septiembre de 2026"
                )
            )
        ),

        Provider(
            id = "silvia-paz",
            name = "Silvia Paz",
            specialty = "Estufas y microondas",
            category = ServiceCategory.APPLIANCES,
            zone = "Zona 18",
            experienceYears = 6,
            rating = 4.6,
            reviewCount = 29,
            isAvailable = true,
            description = "Reparación de estufas de gas y eléctricas, hornos y microondas.",
            coverageZones = listOf("Zona 18", "Zona 17", "Zona 6"),
            contactPhone = "+502 5555-0502",
            contactEmail = "silvia@localhands.gt",
            reviews = listOf(
                CommunityReview(
                    reviewerName = "Rodrigo Chávez",
                    rating = 5,
                    comment = "Reparó la estufa el mismo día. Muy recomendada.",
                    date = "11 de septiembre de 2026"
                )
            )
        )
    )

    // Pedido de ejemplo ya terminado, para poder probar el flujo de calificación.
    val initialRequests: List<ServiceRequest> = listOf(
        ServiceRequest(
            id = "req-1",
            providerId = "carlos-mendez",
            providerName = "Carlos Méndez",
            category = ServiceCategory.ELECTRICAL,
            description = "Reparación de tablero eléctrico",
            zone = "Zona 10",
            date = LocalDate.of(2026, 10, 3),
            timeSlot = TimeSlot.MORNING,
            status = RequestStatus.TO_RATE
        )
    )

    fun getProviderById(providerId: String): Provider? {
        return providers.firstOrNull { provider ->
            provider.id == providerId
        }
    }
}
