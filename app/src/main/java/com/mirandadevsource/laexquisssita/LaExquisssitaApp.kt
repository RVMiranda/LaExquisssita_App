package com.mirandadevsource.laexquisssita

import android.app.Application
import com.mirandadevsource.laexquisssita.core.di.AppContainer

class LaExquisssitaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
