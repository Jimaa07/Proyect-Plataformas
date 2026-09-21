package com.example.proyect_plataformas.ui.screens.request

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.model.Provider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

private const val MIN_PROBLEM_LENGTH = 20

private val DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter
        .ofPattern("dd/MM/uuuu")
        .withResolverStyle(ResolverStyle.STRICT)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceScreen(
    provider: Provider,
    onBackClick: () -> Unit,
    onRequestSubmitted: () -> Unit
) {
    var problemDescription by rememberSaveable {
        mutableStateOf("")
    }

    var zone by rememberSaveable {
        mutableStateOf("")
    }

    var preferredDate by rememberSaveable {
        mutableStateOf("")
    }

    var selectedTimeSlot by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var showConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val descriptionError =
        validateDescription(problemDescription)

    val zoneError =
        validateZone(
            zone = zone,
            availableZones = provider.coverageZones
        )

    val dateError =
        validateDate(preferredDate)

    val timeSlotError =
        if (selectedTimeSlot == null) {
            "Selecciona una franja horaria."
        } else {
            null
        }

    val isFormValid =
        descriptionError == null &&
                zoneError == null &&
                dateError == null &&
                timeSlotError == null

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Solicitar servicio",
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
        RequestServiceContent(
            provider = provider,
            problemDescription = problemDescription,
            zone = zone,
            preferredDate = preferredDate,
            selectedTimeSlot = selectedTimeSlot,
            descriptionError = descriptionError,
            zoneError = zoneError,
            dateError = dateError,
            timeSlotError = timeSlotError,
            isFormValid = isFormValid,
            innerPadding = innerPadding,
            onDescriptionChanged = {
                problemDescription = it
            },
            onZoneChanged = {
                zone = it
            },
            onDateChanged = {
                preferredDate = it
            },
            onTimeSlotSelected = {
                selectedTimeSlot = it
            },
            onSubmitClick = {
                showConfirmation = true
            }
        )
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
            },
            title = {
                Text("Solicitud enviada")
            },
            text = {
                Text(
                    text = "Tu solicitud para ${provider.name} fue registrada " +
                            "correctamente ."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        onRequestSubmitted()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun RequestServiceContent(
    provider: Provider,
    problemDescription: String,
    zone: String,
    preferredDate: String,
    selectedTimeSlot: String?,
    descriptionError: String?,
    zoneError: String?,
    dateError: String?,
    timeSlotError: String?,
    isFormValid: Boolean,
    innerPadding: PaddingValues,
    onDescriptionChanged: (String) -> Unit,
    onZoneChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onTimeSlotSelected: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        ProviderSummaryCard(
            provider = provider
        )

        Text(
            text = "Cuéntanos qué necesitas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = problemDescription,
            onValueChange = onDescriptionChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Descripción del problema")
            },
            placeholder = {
                Text(
                    "Ej. Necesito reparar un tomacorriente que dejó de funcionar."
                )
            },
            minLines = 4,
            maxLines = 6,
            isError = descriptionError != null,
            supportingText = {
                Text(
                    text = descriptionError
                        ?: "${problemDescription.trim().length} caracteres"
                )
            }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = zone,
                onValueChange = onZoneChanged,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Zona")
                },
                placeholder = {
                    Text("Ej. Zona 10")
                },
                singleLine = true,
                isError = zoneError != null,
                supportingText = {
                    if (zoneError != null) {
                        Text(zoneError)
                    } else {
                        Text("Zona válida")
                    }
                }
            )

            if (provider.coverageZones.isNotEmpty()) {
                Text(
                    text = "Zonas disponibles:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    provider.coverageZones.forEach { availableZone ->
                        FilterChip(
                            selected = zone.equals(
                                availableZone,
                                ignoreCase = true
                            ),
                            onClick = {
                                onZoneChanged(availableZone)
                            },
                            label = {
                                Text(availableZone)
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = preferredDate,
            onValueChange = onDateChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Fecha preferida")
            },
            placeholder = {
                Text("dd/MM/aaaa")
            },
            singleLine = true,
            isError = dateError != null,
            supportingText = {
                if (dateError != null) {
                    Text(dateError)
                } else {
                    Text("Fecha válida")
                }
            }
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Franja horaria",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedTimeSlot == "Mañana",
                    onClick = {
                        onTimeSlotSelected("Mañana")
                    },
                    label = {
                        Text("Mañana")
                    },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedTimeSlot == "Tarde",
                    onClick = {
                        onTimeSlotSelected("Tarde")
                    },
                    label = {
                        Text("Tarde")
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (timeSlotError != null) {
                Text(
                    text = timeSlotError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Button(
            onClick = onSubmitClick,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar solicitud")
        }

        if (!isFormValid) {
            Text(
                text = "Completa correctamente todos los campos para habilitar el botón.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProviderSummaryCard(
    provider: Provider
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
    }
}

private fun validateDescription(
    description: String
): String? {
    return when {
        description.isBlank() ->
            "La descripción es obligatoria."

        description.trim().length < MIN_PROBLEM_LENGTH ->
            "La descripción debe tener al menos " +
                    "$MIN_PROBLEM_LENGTH caracteres."

        else -> null
    }
}

private fun validateZone(
    zone: String,
    availableZones: List<String>
): String? {
    if (zone.isBlank()) {
        return "La zona es obligatoria."
    }

    if (
        availableZones.isNotEmpty() &&
        availableZones.none {
            it.equals(
                zone.trim(),
                ignoreCase = true
            )
        }
    ) {
        return "Selecciona una zona de cobertura del profesional."
    }

    return null
}

private fun validateDate(
    dateText: String
): String? {
    if (dateText.isBlank()) {
        return "La fecha es obligatoria."
    }

    return try {
        val selectedDate =
            LocalDate.parse(
                dateText.trim(),
                DATE_FORMATTER
            )

        if (selectedDate.isBefore(LocalDate.now())) {
            "La fecha no puede estar en el pasado."
        } else {
            null
        }
    } catch (_: DateTimeParseException) {
        "Ingresa una fecha válida en formato dd/MM/aaaa."
    }
}