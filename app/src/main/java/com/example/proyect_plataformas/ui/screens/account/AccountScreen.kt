package com.example.proyect_plataformas.ui.screens.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.ui.components.AppBottomBar
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.components.InitialsAvatar
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

/**
 * Pestaña "Perfil": datos del vecino que usa la app y resumen de su actividad.
 * Solo muestra acciones que funcionan (ver pedidos); nada queda decorativo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    user: UserProfile,
    activeRequests: Int,
    pendingRatings: Int,
    reviewsWritten: Int,
    onOrdersClick: () -> Unit,
    onTabSelected: (BottomTab) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mi perfil",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = BottomTab.PROFILE,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                UserHeader(
                    user = user
                )
            }

            item {
                ActivityCard(
                    activeRequests = activeRequests,
                    pendingRatings = pendingRatings,
                    reviewsWritten = reviewsWritten,
                    onOrdersClick = onOrdersClick
                )
            }

            item {
                ContactCard(
                    user = user
                )
            }
        }
    }
}

@Composable
private fun UserHeader(
    user: UserProfile
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InitialsAvatar(
            name = user.name,
            size = 64.dp,
            textStyle = MaterialTheme.typography.headlineSmall
        )

        Column {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Vecino de ${user.zone}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activeRequests: Int,
    pendingRatings: Int,
    reviewsWritten: Int,
    onOrdersClick: () -> Unit
) {
    SectionCard {
        Text(
            text = "Mi actividad",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActivityStat(value = activeRequests, label = "en curso")
            ActivityStat(value = pendingRatings, label = "por calificar")
            ActivityStat(value = reviewsWritten, label = "reseñas")
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline
        )

        Surface(
            onClick = onOrdersClick,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver mis pedidos",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ActivityStat(
    value: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactCard(
    user: UserProfile
) {
    SectionCard {
        Text(
            text = "Datos de contacto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ContactRow(label = "Teléfono", value = user.phone)
        ContactRow(label = "Correo", value = user.email)
        ContactRow(label = "Zona", value = user.zone)
    }
}

@Composable
private fun ContactRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionCard(
    content: @Composable () -> Unit
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
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun AccountScreenPreview() {
    ProyectplataformasTheme {
        AccountScreen(
            user = MockLocalHandsData.currentUser,
            activeRequests = 1,
            pendingRatings = 1,
            reviewsWritten = 0,
            onOrdersClick = {},
            onTabSelected = {}
        )
    }
}
