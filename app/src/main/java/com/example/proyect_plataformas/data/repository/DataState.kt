package com.example.proyect_plataformas.data.repository

/**
 * Lo que entrega el Repository cuando se observa un dato.
 *
 * - Loading: todavía no hay ninguna respuesta (ni del servidor ni de la caché).
 * - Ready: hay datos. isFromCache indica que vienen de la copia guardada en el
 *   dispositivo porque no hay conexión; hasPendingWrites indica que hay cambios
 *   hechos en el dispositivo que todavía no llegan al servidor.
 * - Failure: no se pudieron leer (por ejemplo, las reglas lo impiden).
 */
sealed interface DataState<out T> {

    data object Loading : DataState<Nothing>

    data class Ready<out T>(
        val data: T,
        val isFromCache: Boolean = false,
        val hasPendingWrites: Boolean = false
    ) : DataState<T>

    data class Failure(
        val cause: Throwable
    ) : DataState<Nothing>
}
