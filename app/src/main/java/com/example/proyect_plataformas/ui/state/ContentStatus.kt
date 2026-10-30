package com.example.proyect_plataformas.ui.state

import com.example.proyect_plataformas.data.model.RepositorySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

const val OFFLINE_NOTICE = "Sin conexión: se muestran los datos guardados en el teléfono."
const val PENDING_SYNC_NOTICE =
    "Hay cambios guardados en el teléfono que se enviarán al volver la conexión."

/**
 * Lo que un ViewModel sabe de un dato que observa en el Repository.
 *
 * - Loading: todavía no llegó ninguna respuesta (ni del servidor ni de la caché).
 * - Ready: hay datos. isFromCache indica que vienen de la copia guardada en el
 *   teléfono; hasPendingWrites, que hay cambios que todavía no llegan al servidor.
 * - Failure: no se pudieron leer (sin sesión, reglas, error inesperado).
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

/** Cambia el dato de un Ready y conserva Loading, Failure y los metadatos. */
fun <T, R> DataState<T>.mapData(transform: (T) -> R): DataState<R> {
    return when (this) {
        DataState.Loading -> DataState.Loading
        is DataState.Failure -> this
        is DataState.Ready -> DataState.Ready(
            data = transform(data),
            isFromCache = isFromCache,
            hasPendingWrites = hasPendingWrites
        )
    }
}

/** Estado visible de una lista que depende del Repository. */
sealed interface ContentStatus {

    data object Loading : ContentStatus

    data class Ready(
        val notice: String? = null
    ) : ContentStatus

    data class Error(
        val message: String
    ) : ContentStatus
}

/** Estado visible de una pantalla que muestra un solo elemento (perfil, solicitud). */
sealed interface DetailState<out T> {

    data object Loading : DetailState<Nothing>

    data class Ready<out T>(
        val data: T,
        val notice: String? = null
    ) : DetailState<T>

    data object NotFound : DetailState<Nothing>

    data class Error(
        val message: String
    ) : DetailState<Nothing>
}

fun noticeFor(state: DataState.Ready<*>): String? {
    return when {
        state.isFromCache -> OFFLINE_NOTICE
        state.hasPendingWrites -> PENDING_SYNC_NOTICE
        else -> null
    }
}

fun DataState<*>.toContentStatus(errorMessage: String): ContentStatus {
    return when (this) {
        DataState.Loading -> ContentStatus.Loading
        is DataState.Ready -> ContentStatus.Ready(notice = noticeFor(this))
        is DataState.Failure -> ContentStatus.Error(errorMessage)
    }
}

/**
 * Si el elemento no llega y la respuesta vino del servidor, no existe.
 * Si vino de la caché, solo sabemos que no está guardado en el teléfono.
 */
fun <T : Any> DataState<T?>.toDetailState(
    errorMessage: String,
    offlineMessage: String
): DetailState<T> {
    return when (this) {
        DataState.Loading -> DetailState.Loading
        is DataState.Failure -> DetailState.Error(errorMessage)
        is DataState.Ready -> {
            val item = data

            when {
                item != null -> DetailState.Ready(item, notice = noticeFor(this))
                isFromCache -> DetailState.Error(offlineMessage)
                else -> DetailState.NotFound
            }
        }
    }
}

/**
 * Vuelve a observar el Repository cada vez que cambia el contador de reintentos.
 * Empieza en Loading, convierte cada snapshot en Ready con sus metadatos y
 * una excepción en Failure, para que ninguna pantalla se cierre ni se quede
 * cargando.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Flow<Int>.observeWithRetry(
    source: () -> Flow<RepositorySnapshot<T>>
): Flow<DataState<T>> {
    return flatMapLatest {
        source()
            .map<RepositorySnapshot<T>, DataState<T>> { snapshot ->
                DataState.Ready(
                    data = snapshot.value,
                    isFromCache = snapshot.isFromCache,
                    hasPendingWrites = snapshot.hasPendingWrites
                )
            }
            .onStart { emit(DataState.Loading) }
            .catch { error -> emit(DataState.Failure(error)) }
    }
}
