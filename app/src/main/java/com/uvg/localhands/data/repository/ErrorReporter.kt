package com.uvg.localhands.data.repository

/**
 * Registro de errores no fatales. El Repository depende de esta interface
 * y no de Crashlytics, así las pruebas usan una versión en memoria.
 */
interface ErrorReporter {

    /** [operation] es el nombre de la función del Repository que falló. */
    fun recordNonFatal(operation: String, error: Throwable)
}
