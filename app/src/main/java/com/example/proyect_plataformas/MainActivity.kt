package com.example.proyect_plataformas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.proyect_plataformas.navigation.LocalHandsNavHost
import com.example.proyect_plataformas.ui.theme.ProyectplataformasTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProyectplataformasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LocalHandsNavHost()
                }
            }
        }
    }
}