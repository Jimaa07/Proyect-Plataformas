package com.example.proyect_plataformas.ui.screens.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.model.CompletedService
import com.example.proyect_plataformas.ui.components.StarRatingSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateServiceScreen(
    service: CompletedService,
    onBackClick: () -> Unit,
    onRatingSubmitted: () -> Unit
) {
    var selectedStars by rememberSaveable {
        mutableIntStateOf(0)
    }

    var comment by rememberSaveable {
        mutableStateOf("")
    }

    var showConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val isFormValid = isRatingFormValid(
        stars = selectedStars,
        comment = comment
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LocalHands",
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
        RateServiceContent(
            service = service,
            selectedStars = selectedStars,
            comment = comment,
            isFormValid = isFormValid,
            onStarsSelected = { stars ->
                selectedStars = stars
            },
            onCommentChanged = { newComment ->
                comment = newComment
            },
            onSubmitClick = {
                showConfirmation = true
            },
            innerPadding = innerPadding
        )
    }

    if (showConfirmation) {
        RatingConfirmationDialog(
            onConfirm = {
                showConfirmation = false
                onRatingSubmitted()
            }
        )
    }
}

@Composable
private fun RateServiceContent(
    service: CompletedService,
    selectedStars: Int,
    comment: String,
    isFormValid: Boolean,
    onStarsSelected: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmitClick: () -> Unit,
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                top = 24.dp,
                end = 24.dp,
                bottom = 32.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "¿Cómo estuvo el servicio?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Tu opinión ayuda a otros vecinos a encontrar profesionales confiables.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CompletedServiceCard(
            service = service
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Califica tu experiencia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            StarRatingSelector(
                selectedStars = selectedStars,
                onStarsSelected = onStarsSelected
            )

            Text(
                text = ratingDescription(selectedStars),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChanged,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            label = {
                Text("Cuéntanos sobre tu experiencia")
            },
            placeholder = {
                Text(
                    "El trabajo fue rápido y muy profesional..."
                )
            },
            supportingText = {
                Text(
                    text = reviewSupportingText(comment)
                )
            },
            isError = comment.isNotEmpty() &&
                    comment.trim().length < MINIMUM_REVIEW_LENGTH,
            minLines = 4,
            maxLines = 5
        )

        Button(
            onClick = onSubmitClick,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar reseña")
        }

        if (!isFormValid) {
            Text(
                text = "Selecciona una calificación y escribe un comentario para habilitar el botón.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun CompletedServiceCard(
    service: CompletedService
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = service.providerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = service.serviceDescription,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = service.completedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RatingConfirmationDialog(
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("¡Gracias por tu reseña!")
        },
        text = {
            Text(
                "Tu opinión fue registrada correctamente y ayudará a otros vecinos."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Aceptar")
            }
        }
    )
}

private fun ratingDescription(
    selectedStars: Int
): String {
    return when (selectedStars) {
        1 -> "Muy mala experiencia"
        2 -> "Podría mejorar"
        3 -> "Experiencia aceptable"
        4 -> "Muy buen servicio"
        5 -> "Excelente servicio"
        else -> "Selecciona de 1 a 5 estrellas"
    }
}

private fun reviewSupportingText(
    comment: String
): String {
    val currentLength = comment.trim().length

    return if (currentLength < MINIMUM_REVIEW_LENGTH) {
        "Mínimo $MINIMUM_REVIEW_LENGTH caracteres: " +
                "$currentLength/$MINIMUM_REVIEW_LENGTH"
    } else {
        "Comentario válido"
    }
}