package com.example.proyect_plataformas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.ui.theme.LocalHandsStar

@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 12.dp,
                        top = 12.dp,
                        end = 12.dp,
                        bottom = 10.dp
                    ),
                verticalAlignment = Alignment.Top
            ) {

                ProviderAvatar(
                    provider = provider
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "★ ${provider.rating}",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalHandsStar,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    SpecialtyBadge(
                        specialty = provider.specialty
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SmallInfoBadge(
                            text = "⌖ ${provider.zone}"
                        )

                        SmallInfoBadge(
                            text = "▣ ${provider.experienceYears} años exp."
                        )
                    }

                    SmallInfoBadge(
                        text = "▤ ${provider.reviewCount} reseñas"
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                AvailabilityBadge(
                    isAvailable = provider.isAvailable
                )

                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProviderAvatar(
    provider: Provider
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = providerInitials(provider.name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SpecialtyBadge(
    specialty: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 7.dp,
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "⚡",
                style = MaterialTheme.typography.labelSmall
            )

            Text(
                text = specialty,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SmallInfoBadge(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 3.dp
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AvailabilityBadge(
    isAvailable: Boolean
) {
    val text = if (isAvailable) {
        "DISPONIBLE AHORA"
    } else {
        "OCUPADO HOY"
    }

    val background = if (isAvailable) {
        Color(0xFF075E4E)
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
            text = text,
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

private fun providerInitials(
    name: String
): String {
    return name
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { word ->
            word.firstOrNull()?.uppercaseChar()
        }
        .joinToString("")
}