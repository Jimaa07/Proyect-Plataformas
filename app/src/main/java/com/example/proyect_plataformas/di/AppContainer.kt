package com.example.proyect_plataformas.di

import com.example.proyect_plataformas.data.mock.MockLocalHandsRepository
import com.example.proyect_plataformas.data.repository.LocalHandsRepository

/**
 * Crea una sola vez las implementaciones que usa la app (inyección manual).
 * Los ViewModels reciben el Repository por constructor desde su Factory.
 *
 * Mientras no esté la versión con Firestore, se usa la implementación en memoria.
 * Para cambiarla basta con reemplazar esta línea; nada más de la app cambia.
 */
class AppContainer {

    val localHandsRepository: LocalHandsRepository = MockLocalHandsRepository()
}
