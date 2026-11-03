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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyect_plataformas.data.mock.MockLocalHandsData
import com.example.proyect_plataformas.data.model.UserProfile
import com.example.proyect_plataformas.ui.components.AppBottomBar
import com.example.proyect_plataformas.ui.components.BottomTab
import com.example.proyect_plataformas.ui.components.ErrorMessage
import com.example.proyect_plataformas.ui.components.InitialsAvatar
import com.example.proyect_plataformas.ui.components.LoadingIndicator
import com.example.proyect_plataformas.ui.components.NoticeBanner
import com.example.proyect_plataformas.ui.state.ContentStatus
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

/**
 * Pestaña "Perfil": datos del vecino guardados en Firestore y resumen de su
 * actividad. Si todavía no hay perfil, se muestra el formulario para crearlo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountUiState,
    onEditProfile: () -> Unit,
    onCancelEdit: () -> Unit,
    onNameChanged: (String) -> Unit,
    onZoneChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onRetry: () -> Unit,
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
            when (val status = state.profileStatus) {
                ContentStatus.Loading -> {
                    item {
                        LoadingIndicator(
                            message = "Cargando tu perfil..."
                        )
                    }
                }

                is ContentStatus.Error -> {
                    item {
                        ErrorMessage(
                            message = status.message,
                            onRetry = onRetry
                        )
                    }
                }

                is ContentStatus.Ready -> {
                    status.notice?.let { notice ->
                        item {
                            NoticeBanner(
                                text = notice
                            )
                        }
                    }

                    val form = state.form
                    val profile = state.profile

                    if (form != null) {
                        item {
                            ProfileFormCard(
                                form = form,
                                errors = state.formErrors,
                                isNewProfile = profile == null,
                                onNameChanged = onNameChanged,
                                onZoneChanged = onZoneChanged,
                                onPhoneChanged = onPhoneChanged,
                                onEmailChanged = onEmailChanged,
                                onSave = onSaveProfile,
                                onCancel = onCancelEdit
                            )
                        }
                    } else if (profile != null) {
                        item {
                            UserHeader(
                                user = profile,
                                onEditProfile = onEditProfile
                            )
                        }
                    }

                    item {
                        ActivityCard(
                            activeRequests = state.activeRequests,
                            pendingRatings = state.pendingRatings,
                            reviewsWritten = state.reviewsWritten,
                            onOrdersClick = onOrdersClick
                        )
                    }

                    if (form == null && profile != null) {
                        item {
                            ContactCard(
                                user = profile
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Crear o editar el perfil. Teléfono y correo son opcionales; los errores
 * aparecen después del primer intento de guardar.
 */
@Composable
private fun ProfileFormCard(
    form: ProfileFormState,
    errors: ProfileFormErrors,
    isNewProfile: Boolean,
    onNameChanged: (String) -> Unit,
    onZoneChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val canEdit = !form.isSaving

    SectionCard {
        Text(
            text = if (isNewProfile) "Completa tu perfil" else "Editar perfil",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (isNewProfile) {
            Text(
                text = "Tu nombre aparece en el saludo y firma las reseñas que escribas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProfileTextField(
            value = form.name,
            onValueChange = onNameChanged,
            label = "Nombre",
            error = errors.name.takeIf { form.showErrors },
            enabled = canEdit
        )

        ProfileTextField(
            value = form.zone,
            onValueChange = onZoneChanged,
            label = "Zona",
            error = errors.zone.takeIf { form.showErrors },
            enabled = canEdit,
            placeholder = "Ej. Zona 10"
        )

        ProfileTextField(
            value = form.phone,
            onValueChange = onPhoneChanged,
            label = "Teléfono (opcional)",
            error = errors.phone.takeIf { form.showErrors },
            enabled = canEdit,
            keyboardType = KeyboardType.Phone,
            placeholder = "Ej. 5555-0101"
        )

        ProfileTextField(
            value = form.email,
            onValueChange = onEmailChanged,
            label = "Correo (opcional)",
            error = errors.email.takeIf { form.showErrors },
            enabled = canEdit,
            keyboardType = KeyboardType.Email
        )

        form.saveError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isNewProfile) {
                TextButton(
                    onClick = onCancel,
                    enabled = canEdit
                ) {
                    Text("Cancelar")
                }
            }

            Button(
                onClick = onSave,
                enabled = canEdit
            ) {
                Text(if (form.isSaving) "Guardando..." else "Guardar perfil")
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(label) },
        placeholder = if (placeholder != null) {
            { Text(placeholder) }
        } else {
            null
        },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun UserHeader(
    user: UserProfile,
    onEditProfile: () -> Unit
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

            TextButton(
                onClick = onEditProfile
            ) {
                Text("Editar perfil")
            }
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

        ContactRow(label = "Teléfono", value = user.phone.ifBlank { "Sin registrar" })
        ContactRow(label = "Correo", value = user.email.ifBlank { "Sin registrar" })
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
            state = AccountUiState(
                profileStatus = ContentStatus.Ready(),
                profile = MockLocalHandsData.currentUser,
                activeRequests = 1,
                pendingRatings = 1
            ),
            onEditProfile = {},
            onCancelEdit = {},
            onNameChanged = {},
            onZoneChanged = {},
            onPhoneChanged = {},
            onEmailChanged = {},
            onSaveProfile = {},
            onRetry = {},
            onOrdersClick = {},
            onTabSelected = {}
        )
    }
}
