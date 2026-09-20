package com.example.proyect_plataformas.ui.screens.profile


import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    provider: Provider,
    onBackClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    var contactMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Perfil profesional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        ProfileContent(
            provider = provider,
            innerPadding = innerPadding,
            contactMessage = contactMessage,
            onPhoneClick = {
                contactMessage =
                    "Contacto mock: llamar a ${provider.contactPhone}"
            },
            onEmailClick = {
                contactMessage =
                    "Contacto mock: escribir a ${provider.contactEmail}"
            },
            onRequestServiceClick = onRequestServiceClick
        )
    }
}

@Composable
private fun ProfileContent(
    provider: Provider,
    innerPadding: PaddingValues,
    contactMessage: String?,
    onPhoneClick: () -> Unit,
    onEmailClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        item {
            ProviderHeader(
                provider = provider
            )
        }

        item {
            RatingSection(
                provider = provider
            )
        }

        item {
            ProfileSection(
                title = "Experiencia"
            ) {
                Text(
                    text = "${provider.experienceYears} años de experiencia",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        item {
            ProfileSection(
                title = "Zonas de cobertura"
            ) {
                CoverageZones(
                    zones = provider.coverageZones.ifEmpty {
                        listOf(provider.zone)
                    }
                )
            }
        }

        item {
            ProfileSection(
                title = "Descripción profesional"
            ) {
                Text(
                    text = provider.description.ifBlank {
                        "Profesional de LocalHands."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            ProfileSection(
                title = "Contacto"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPhoneClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Llamar")
                        }

                        OutlinedButton(
                            onClick = onEmailClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Correo")
                        }
                    }

                    if (contactMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = contactMessage,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Reseñas comunitarias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (provider.reviews.isEmpty()) {
            item {
                Text(
                    text = "Este profesional todavía no tiene reseñas visibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = provider.reviews
            ) { review ->
                ReviewCard(
                    review = review
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Button(
                onClick = onRequestServiceClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solicitar servicio")
            }
        }
    }
}

@Composable
private fun ProviderHeader(
    provider: Provider
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // Avatar mock. No backend ni imágenes remotas todavía.
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = providerInitials(provider.name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AvailabilityBadge(
            isAvailable = provider.isAvailable
        )

        Text(
            text = provider.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = provider.specialty,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun RatingSection(
    provider: Provider
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFA000)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = provider.rating.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "(${provider.reviewCount} reseñas)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CoverageZones(
    zones: List<String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        zones.forEach { zone ->
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = zone,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        content()
    }
}

@Composable
private fun ReviewCard(
    review: CommunityReview
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.reviewerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "★ ${review.rating}/5",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFA000)
                )
            }

            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = review.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AvailabilityBadge(
    isAvailable: Boolean
) {
    val backgroundColor = if (isAvailable) {
        Color(0xFFD2F4EA)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isAvailable) {
        Color(0xFF00513F)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val text = if (isAvailable) {
        "DISPONIBLE"
    } else {
        "NO DISPONIBLE"
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

private fun providerInitials(
    name: String
): String {
    return name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { word ->
            word.first().uppercaseChar().toString()
        }
}