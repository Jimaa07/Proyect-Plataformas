package com.example.proyect_plataformas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp

@Composable
fun StarRatingSelector(
    selectedStars: Int,
    onStarsSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..5).forEach { star ->
            IconButton(
                onClick = {
                    onStarsSelected(star)
                },
                modifier = Modifier.semantics {
                    contentDescription = "$star estrellas"
                }
            ) {
                Text(
                    text = if (star <= selectedStars) "★" else "☆",
                    color = Color(0xFFFFA000),
                    fontSize = 36.sp
                )
            }
        }
    }
}