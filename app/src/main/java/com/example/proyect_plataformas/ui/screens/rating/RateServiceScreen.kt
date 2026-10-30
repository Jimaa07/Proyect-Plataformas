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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.model.CompletedService
import com.example.proyect_plataformas.ui.components.StarRatingSelector
import com.example.proyect_plataformas.ui.state.RatingFormState
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateServiceScreen(
    service: CompletedService,
    form: RatingFormState,
    onBackClick: () -> Unit,
    onStarsSelected: (Int) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onFinished: () -> Unit
) {
    // Las estrellas, el comentario y la validación viven en el ViewModel;
    // esta pantalla solo los muestra y avisa cada cambio.
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Calificar servicio",
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
            selectedStars = form.stars,
            comment = form.comment,
            isFormValid = form.isValid && !form.submitted,
            isSubmitting = form.isSubmitting,
            submitError = form.submitError,
            onStarsSelected = onStarsSelected,
            onCommentChanged = onCommentChanged,
            onSubmitClick = onSubmitClick,
            innerPadding = innerPadding
        )
    }

    // El ViewModel ya guardó la reseña; el diálogo solo confirma y regresa.
    if (form.submitted) {
        RatingConfirmationDialog(
            onConfirm = onFinished
        )
    }
}

@Composable
private fun RateServiceContent(
    service: CompletedService,
    selectedStars: Int,
    comment: String,
    isFormValid: Boolean,
    isSubmitting: Boolean,
    submitError: String?,
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
            enabled = isFormValid && !isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSubmitting) "Enviando..." else "Enviar reseña"
            )
        }

        submitError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
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

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RateServiceScreenPreview() {
    ProyectplataformasTheme {
        RateServiceScreen(
            service = CompletedService(
                id = "req-1",
                providerId = "carlos-mendez",
                providerName = "Carlos Méndez",
                serviceDescription = "Reparación de tablero eléctrico",
                completedDate = "3 de octubre de 2026"
            ),
            form = RatingFormState(
                requestId = "req-1",
                stars = 4,
                comment = "Llegó puntual y explicó todo"
            ),
            onBackClick = {},
            onStarsSelected = {},
            onCommentChanged = {},
            onSubmitClick = {},
            onFinished = {}
        )
    }
}
