package com.example.proyect_plataformas.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.RequestStatus
import com.example.proyect_plataformas.data.model.ServiceRequest
import com.example.proyect_plataformas.ui.components.AppBottomBar
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.format.formatDayAndDate
import com.example.proyect_plataformas.ui.format.starsText
import com.example.proyect_plataformas.ui.theme.LocalHandsStar
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

/** Pestaña "Mis pedidos": solicitudes enviadas y trabajos por calificar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    requests: List<ServiceRequest>,
    onRequestClick: (providerId: String) -> Unit,
    onRateClick: (requestId: String) -> Unit,
    onExploreClick: () -> Unit,
    onTabSelected: (BottomTab) -> Unit
) {
    val orderedRequests = ordersForDisplay(requests)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Mis pedidos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                selectedTab = BottomTab.ORDERS,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (orderedRequests.isEmpty()) {
                item {
                    EmptyOrders(
                        onExploreClick = onExploreClick
                    )
                }
            } else {
                items(
                    items = orderedRequests,
                    key = { request -> request.id }
                ) { request ->
                    OrderCard(
                        request = request,
                        onClick = { onRequestClick(request.providerId) },
                        onRateClick = { onRateClick(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    request: ServiceRequest,
    onClick: () -> Unit,
    onRateClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${request.category.symbol} ${request.providerName}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                StatusBadge(
                    status = request.status
                )
            }

            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${formatDayAndDate(request.date)} · ${request.timeSlot.label} " +
                        "(${request.timeSlot.hours}) · ${request.zone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when (request.status) {
                RequestStatus.TO_RATE -> {
                    Button(
                        onClick = onRateClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calificar trabajo")
                    }
                }

                RequestStatus.RATED -> {
                    Text(
                        text = "Tu calificación: ${starsText(request.rating ?: 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalHandsStar
                    )
                }

                RequestStatus.SENT -> {
                    Text(
                        text = "Esperando confirmación del profesional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: RequestStatus
) {
    val (background, foreground) = when (status) {
        RequestStatus.TO_RATE ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer

        RequestStatus.SENT ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant

        RequestStatus.RATED ->
            Color(0xFFE3F4E6) to Color(0xFF1E6B2D)
    }

    Surface(
        color = background,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = status.label,
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

@Composable
private fun EmptyOrders(
    onExploreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Todavía no tienes pedidos.",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "Cuando solicites un servicio, aquí verás su estado.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onExploreClick
        ) {
            Text("Buscar un profesional")
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun OrdersScreenPreview() {
    ProyectplataformasTheme {
        OrdersScreen(
            requests = MockLocalHandsData.initialRequests,
            onRequestClick = {},
            onRateClick = {},
            onExploreClick = {},
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 500)
@Composable
private fun OrdersScreenEmptyPreview() {
    ProyectplataformasTheme {
        OrdersScreen(
            requests = emptyList(),
            onRequestClick = {},
            onRateClick = {},
            onExploreClick = {},
            onTabSelected = {}
        )
    }
}
