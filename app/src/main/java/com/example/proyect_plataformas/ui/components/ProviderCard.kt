package com.example.proyect_plataformas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = provider.specialty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "★ ${provider.rating}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFA000),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProviderInformationBadge(
                    text = provider.zone
                )

                ProviderInformationBadge(
                    text = "${provider.experienceYears} años exp."
                )
            }

            ProviderInformationBadge(
                text = "${provider.reviewCount} reseñas"
            )

            AvailabilityBadge(
                isAvailable = provider.isAvailable
            )
        }
    }
}

@Composable
private fun ProviderInformationBadge(
    text: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

    val availabilityText = if (isAvailable) {
        "DISPONIBLE AHORA"
    } else {
        "OCUPADO HOY"
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = availabilityText,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}