package com.example.proyect_plataformas

import android.app.Application
import com.example.proyect_plataformas.di.AppContainer

/** Crea el AppContainer una sola vez para toda la app. */
class LocalHandsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
