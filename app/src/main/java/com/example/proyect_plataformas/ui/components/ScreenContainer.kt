package com.example.proyect_plataformas.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun ScreenContainer(
    title: String = "LocalHands",
    onHomeClick: () -> Unit,
    onRateServiceClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = title) },
        bottomBar = {
            AppBottomBar(
                onHomeClick = onHomeClick,
                onRateServiceClick = onRateServiceClick
            )
        },
        content = content
    )
}