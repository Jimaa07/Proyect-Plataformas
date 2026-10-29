package com.example.proyect_plataformas.data.model

data class CommunityReview(
    val id: String = "",
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class Provider(
    val id: String,
    val name: String,
    val specialty: String,
    // Categoría del inicio a la que pertenece (sirve para filtrar Resultados).
    val category: ServiceCategory,
    val zone: String,
    val experienceYears: Int,
    val rating: Double,
    val reviewCount: Int,
    val isAvailable: Boolean,

    // Información adicional para la pantalla de perfil.
    // Tienen valores por defecto para no romper código o tests
    // que todavía creen Provider con los campos anteriores.
    val description: String = "",
    val coverageZones: List<String> = emptyList(),
    val contactPhone: String = "",
    val contactEmail: String = "",
    val reviews: List<CommunityReview> = emptyList()
)
