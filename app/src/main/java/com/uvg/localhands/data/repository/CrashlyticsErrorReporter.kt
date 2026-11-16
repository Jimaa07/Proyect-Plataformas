package com.uvg.localhands.data.repository

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsErrorReporter(
    private val crashlytics: FirebaseCrashlytics
) : ErrorReporter {

    override fun recordNonFatal(operation: String, error: Throwable) {
        // La clave aparece en el evento de la consola y dice qué operación falló.
        crashlytics.setCustomKey("repository_operation", operation)
        crashlytics.log("Falló $operation: ${error.message}")
        crashlytics.recordException(error)
    }
}
