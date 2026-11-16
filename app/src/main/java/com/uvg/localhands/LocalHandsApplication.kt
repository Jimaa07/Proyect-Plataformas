package com.uvg.localhands

import android.app.Application
import com.uvg.localhands.di.AppContainer

class LocalHandsApplication : Application() {

    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }
}
