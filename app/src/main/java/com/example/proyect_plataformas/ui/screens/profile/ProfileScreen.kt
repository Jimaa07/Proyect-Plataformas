package com.example.proyect_plataformas.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.ui.theme.LocalHandsStar

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
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LocalHands",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },

                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Text(
                            text = "○",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
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

            onWhatsAppClick = {
                contactMessage =
                    "Contacto mock por WhatsApp: ${provider.contactPhone}"
            },

            onPhoneClick = {
                contactMessage =
                    "Contacto mock: llamar a ${provider.contactPhone}"
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
    onWhatsAppClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),

        contentPadding = PaddingValues(
            start = 14.dp,
            top = 12.dp,
            end = 14.dp,
            bottom = 28.dp
        ),

        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            ProviderMainCard(
                provider = provider,
                onRequestServiceClick = onRequestServiceClick,
                onWhatsAppClick = onWhatsAppClick,
                onPhoneClick = onPhoneClick
            )
        }

        if (contactMessage != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = contactMessage,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Text(
                text = "👍 Recomendado por vecinos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (provider.reviews.isEmpty()) {
            item {
                Text(
                    text = "Este profesional todavía no tiene reseñas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(
                items = provider.reviews
            ) { review ->
                CommunityReviewCard(
                    review = review
                )
            }
        }
    }
}

@Composable
private fun ProviderMainCard(
    provider: Provider,
    onRequestServiceClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onPhoneClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(12.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = providerInitials(provider.name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            AvailabilityBadge(
                isAvailable = provider.isAvailable
            )

            Text(
                text = provider.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "⚡ ${provider.specialty}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ProviderStats(
                provider = provider
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = provider.description.ifBlank {
                    "Profesional de LocalHands."
                },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Button(
                onClick = onRequestServiceClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "▣  Solicitar servicio"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "▣ WhatsApp",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedButton(
                    onClick = onPhoneClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "☎ Llamar",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderStats(
    provider: Provider
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "★",
                color = LocalHandsStar,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = " ${provider.rating}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = " (${provider.reviewCount} reseñas)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "   ⌖ ${providerZones(provider)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "▣ ${provider.experienceYears} años exp.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommunityReviewCard(
    review: CommunityReview
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(10.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),

        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reviewerInitial(review.reviewerName),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = review.reviewerName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = review.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "★".repeat(review.rating),
                    color = LocalHandsStar,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = "“${review.comment}”",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun AvailabilityBadge(
    isAvailable: Boolean
) {
    val background =
        if (isAvailable) {
            Color(0xFF45B95C)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    val foreground =
        if (isAvailable) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        color = background,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = if (isAvailable) {
                "● Disponible"
            } else {
                "No disponible"
            },

            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 4.dp
            ),

            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun providerZones(
    provider: Provider
): String {
    val zones =
        provider.coverageZones.ifEmpty {
            listOf(provider.zone)
        }

    return zones
        .map {
            it.replace(
                "Zona ",
                ""
            )
        }
        .joinToString(", ")
}

private fun providerInitials(
    name: String
): String {
    return name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull {
            it.firstOrNull()?.uppercaseChar()
        }
        .joinToString("")
}

private fun reviewerInitial(
    name: String
): String {
    return name
        .trim()
        .firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "?"
}