package com.example.proyect_plataformas.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.CommunityReview
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.ui.components.InitialsAvatar
import com.example.proyect_plataformas.ui.components.NoticeBanner
import com.example.proyect_plataformas.ui.format.starsText
import com.example.proyect_plataformas.ui.theme.LocalHandsStar
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    provider: Provider,
    onBackClick: () -> Unit,
    onRequestServiceClick: () -> Unit,
    notice: String? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // El contacto todavía es simulado: se avisa con un Snackbar en lugar de
    // ocupar espacio fijo en la pantalla.
    fun showContactMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Perfil del profesional",
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
                }
            )
        },
        // Acción principal anclada abajo: siempre visible y sin competir
        // con el contenido del perfil.
        bottomBar = {
            ProfileActionBar(
                isAvailable = provider.isAvailable,
                onWhatsAppClick = {
                    showContactMessage("WhatsApp con ${provider.name}: ${provider.contactPhone} (simulado)")
                },
                onPhoneClick = {
                    showContactMessage("Llamando a ${provider.contactPhone} (simulado)")
                },
                onRequestServiceClick = onRequestServiceClick
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        ProfileContent(
            provider = provider,
            notice = notice,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun ProfileContent(
    provider: Provider,
    notice: String?,
    innerPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        notice?.let { text ->
            item {
                NoticeBanner(
                    text = text
                )
            }
        }

        item {
            ProviderHeaderCard(
                provider = provider
            )
        }

        item {
            Text(
                text = "Recomendado por vecinos (${provider.reviews.size})",
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
                items = provider.reviews,
                key = { review -> review.id }
            ) { review ->
                CommunityReviewCard(
                    review = review
                )
            }
        }
    }
}

@Composable
private fun ProviderHeaderCard(
    provider: Provider
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Foto, nombre y especialidad en una fila: ocupa la mitad del alto
            // que el diseño anterior centrado.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InitialsAvatar(
                    name = provider.name,
                    size = 68.dp,
                    textStyle = MaterialTheme.typography.headlineSmall
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${provider.category.symbol} ${provider.specialty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AvailabilityBadge(
                        isAvailable = provider.isAvailable
                    )
                }
            }

            ProviderStatsRow(
                provider = provider
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline
            )

            Text(
                text = provider.description.ifBlank {
                    "Profesional de LocalHands."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Zonas que cubre: ${providerZones(provider)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProviderStatsRow(
    provider: Provider
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            value = "★ ${provider.rating}",
            label = "calificación",
            valueColor = LocalHandsStar
        )

        StatItem(
            value = provider.reviewCount.toString(),
            label = "reseñas"
        )

        StatItem(
            value = "${provider.experienceYears} años",
            label = "experiencia"
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Barra inferior del perfil: contacto como botones secundarios pequeños y
 * "Solicitar servicio" como único botón relleno (acción primaria).
 */
@Composable
private fun ProfileActionBar(
    isAvailable: Boolean,
    onWhatsAppClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onRequestServiceClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!isAvailable) {
                Text(
                    text = "No está disponible hoy. Puedes agendar para otro día.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContactButton(
                    symbol = "💬",
                    description = "WhatsApp",
                    onClick = onWhatsAppClick
                )

                ContactButton(
                    symbol = "☎",
                    description = "Llamar",
                    onClick = onPhoneClick
                )

                Button(
                    onClick = onRequestServiceClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Solicitar servicio",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactButton(
    symbol: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .semantics { contentDescription = description },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = symbol,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
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
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = review.reviewerName.trim().firstOrNull()?.uppercase() ?: "?",
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
                    text = starsText(review.rating),
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
    val background = if (isAvailable) {
        Color(0xFF45B95C)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val foreground = if (isAvailable) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = if (isAvailable) "● Disponible" else "No disponible",
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 3.dp
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
    return provider.coverageZones
        .ifEmpty { listOf(provider.zone) }
        .joinToString(", ")
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun ProfileScreenPreview() {
    ProyectplataformasTheme {
        ProfileScreen(
            provider = MockLocalHandsData.providers.first(),
            onBackClick = {},
            onRequestServiceClick = {}
        )
    }
}
