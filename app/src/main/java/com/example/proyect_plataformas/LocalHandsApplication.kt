package com.example.proyect_plataformas

import android.app.Application
import com.example.proyect_plataformas.di.AppContainer

class LocalHandsApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer()
    }
}