package com.example.proyect_plataformas.ui.screens.request

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.Provider
import com.example.proyect_plataformas.data.model.TimeSlot
import com.example.proyect_plataformas.ui.components.LoadingIndicator
import com.example.proyect_plataformas.ui.components.NoticeBanner
import com.example.proyect_plataformas.ui.format.formatDayAndDate
import com.example.proyect_plataformas.ui.format.formatLongDate
import com.example.proyect_plataformas.ui.state.RequestFormState
import com.example.proyect_plataformas.ui.state.coverageZonesOf
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceScreen(
    provider: Provider,
    form: RequestFormState,
    formErrors: RequestFormErrors,
    today: LocalDate,
    onBackClick: () -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onZoneSelected: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSlotSelected: (TimeSlot) -> Unit,
    onSaveDraftClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onFinished: () -> Unit,
    notice: String? = null
) {
    // El formulario y su validación viven en el ViewModel (semana 11).
    // Aquí solo queda lo efímero: si el calendario está abierto.
    var showDatePicker by rememberSaveable {
        mutableStateOf(false)
    }

    // La zona y el horario se eligen con chips: el usuario no tiene que adivinar texto.
    val coverageZones = remember(provider) {
        coverageZonesOf(provider)
    }

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
            coverageZones = coverageZones,
            form = form,
            formErrors = formErrors,
            canSubmit = formErrors.isValid && provider.isAvailable,
            notice = notice,
            innerPadding = innerPadding,
            onDescriptionChanged = onDescriptionChanged,
            onZoneSelected = onZoneSelected,
            onDateClick = { showDatePicker = true },
            onTimeSlotSelected = onTimeSlotSelected,
            onSaveDraftClick = onSaveDraftClick,
            onSubmitClick = onSubmitClick
        )
    }

    if (showDatePicker) {
        RequestDatePickerDialog(
            initialDateMillis = form.date?.let(::localDateToUtcMillis),
            today = today,
            onDateConfirmed = { millis ->
                onDateSelected(utcMillisToLocalDate(millis))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // El ViewModel marca el formulario como enviado; la pantalla solo lo muestra.
    if (form.submitted) {
        val dateText = form.date?.let { formatLongDate(it) }.orEmpty()
        val slotText = form.timeSlot?.label?.lowercase().orEmpty()

        AlertDialog(
            onDismissRequest = onFinished,
            title = {
                Text("Solicitud enviada")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Le enviamos tu solicitud a ${provider.name} para el " +
                                "$dateText en la $slotText. Puedes ver su estado en Mis pedidos."
                    )
                    form.draftError?.let { message ->
                        Text(text = message, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onFinished
                ) {
                    Text("Ver mis pedidos")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestDatePickerDialog(
    initialDateMillis: Long?,
    today: LocalDate,
    onDateConfirmed: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // El calendario solo deja tocar fechas válidas (desde hoy hasta 60 días).
    val selectableDates = remember(today) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return isSelectableRequestDate(utcTimeMillis, today)
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year in today.year..today.plusDays(MAX_DAYS_AHEAD).year
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onDateConfirmed)
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}

@Composable
private fun RequestServiceContent(
    provider: Provider,
    coverageZones: List<String>,
    form: RequestFormState,
    formErrors: RequestFormErrors,
    canSubmit: Boolean,
    notice: String?,
    innerPadding: PaddingValues,
    onDescriptionChanged: (String) -> Unit,
    onZoneSelected: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeSlotSelected: (TimeSlot) -> Unit,
    onSaveDraftClick: () -> Unit,
    onSubmitClick: () -> Unit
) {
    val canEdit = !form.isLoadingDraft && !form.isSavingDraft && !form.isSubmitting && !form.submitted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                top = 16.dp,
                end = 20.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        ProviderSummaryCard(
            provider = provider
        )

        notice?.let { message -> NoticeBanner(text = message) }

        if (form.isLoadingDraft) {
            LoadingIndicator(message = "Recuperando borrador...")
        }

        // ---------- Descripción ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FieldTitle(
                title = "¿Qué necesitas?"
            )

            val descriptionLength = form.description.trim().length

            // El error solo aparece cuando el usuario ya empezó a escribir.
            val showDescriptionError =
                form.description.isNotEmpty() && formErrors.description != null

            OutlinedTextField(
                value = form.description,
                onValueChange = onDescriptionChanged,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Ej. El tomacorriente de la cocina dejó de funcionar desde ayer.")
                },
                minLines = 3,
                maxLines = 6,
                isError = showDescriptionError,
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when {
                                showDescriptionError -> formErrors.description.orEmpty()
                                form.description.isEmpty() ->
                                    "Incluye qué falla, desde cuándo y en qué parte de la casa."
                                else -> "Descripción completa."
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (descriptionLength < MIN_PROBLEM_LENGTH) {
                            Text("$descriptionLength/$MIN_PROBLEM_LENGTH")
                        }
                    }
                }
            )
        }

        // ---------- Zona ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FieldTitle(
                title = "¿Dónde?"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                coverageZones.forEach { availableZone ->
                    FilterChip(
                        selected = form.zone.equals(availableZone, ignoreCase = true),
                        onClick = { onZoneSelected(availableZone) },
                        enabled = canEdit,
                        label = { Text(availableZone) }
                    )
                }
            }

            FieldHint(
                text = if (form.zone == null) {
                    "${provider.name} trabaja solo en estas zonas. Elige una."
                } else {
                    formErrors.zone ?: "La visita será en ${form.zone}."
                },
                isError = form.zone != null && formErrors.zone != null
            )
        }

        // ---------- Fecha ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FieldTitle(
                title = "¿Cuándo?"
            )

            DateSelectorField(
                selectedDate = form.date,
                isError = form.date != null && formErrors.date != null,
                enabled = canEdit,
                onClick = onDateClick
            )

            FieldHint(
                text = if (form.date == null) {
                    "Toca para abrir el calendario. Puedes agendar hasta $MAX_DAYS_AHEAD días adelante."
                } else {
                    formErrors.date ?: "Fecha disponible."
                },
                isError = form.date != null && formErrors.date != null
            )
        }

        // ---------- Horario ----------
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FieldTitle(
                title = "¿En qué horario?"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimeSlot.entries.forEach { slot ->
                    FilterChip(
                        selected = form.timeSlot == slot,
                        onClick = { onTimeSlotSelected(slot) },
                        enabled = canEdit,
                        label = {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = slot.label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = slot.hours,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onSaveDraftClick,
            enabled = canEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (form.isSavingDraft) "Guardando borrador..." else "Guardar borrador")
        }

        form.draftMessage?.let { message -> NoticeBanner(text = message) }
        form.draftError?.let { message ->
            Text(text = message, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onSubmitClick,
            enabled = canSubmit && canEdit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (form.isSubmitting) "Enviando..." else "Enviar solicitud"
            )
        }

        form.submitError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        // En lugar de un mensaje genérico, se lista exactamente qué falta.
        if (!formErrors.isValid) {
            PendingFieldsCard(
                messages = formErrors.pendingMessages
            )
        }
    }
}

@Composable
private fun DateSelectorField(
    selectedDate: LocalDate?,
    isError: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📅",
                fontSize = 20.sp
            )

            Text(
                text = selectedDate?.let { formatDayAndDate(it) } ?: "Elegir fecha",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedDate == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Text(
                text = if (selectedDate == null) "Abrir" else "Cambiar",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FieldTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun FieldHint(
    text: String,
    isError: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

@Composable
private fun PendingFieldsCard(
    messages: List<String>
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Para enviar la solicitud falta:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            messages.forEach { message ->
                Text(
                    text = "• $message",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                text = "${provider.category.symbol} ${provider.specialty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RequestServiceScreenPreview() {
    val today = LocalDate.now()
    val provider = MockLocalHandsData.providers.first()
    val form = RequestFormState(provider.id)

    ProyectplataformasTheme {
        RequestServiceScreen(
            provider = provider,
            form = form,
            formErrors = validateRequestForm(
                description = form.description,
                zone = form.zone,
                date = form.date,
                timeSlot = form.timeSlot,
                coverageZones = coverageZonesOf(provider),
                providerName = provider.name,
                today = today
            ),
            today = today,
            onBackClick = {},
            onDescriptionChanged = {},
            onZoneSelected = {},
            onDateSelected = {},
            onTimeSlotSelected = {},
            onSaveDraftClick = {},
            onSubmitClick = {},
            onFinished = {}
        )
    }
}
