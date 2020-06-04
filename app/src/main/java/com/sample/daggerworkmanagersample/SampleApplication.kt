package com.sample.daggerworkmanagersample

import android.app.Application
import androidx.work.Configuration

class SampleApplication : Application(), Configuration.Provider {

    private lateinit var sampleComponent: SampleComponent

    override fun onCreate() {
        sampleComponent = DaggerSampleComponent.create()
        sampleComponent.inject(this)
        super.onCreate()
    }

    override fun getWorkManagerConfiguration(): Configuration {
        val factory: SampleWorkerFactory = sampleComponent.factory()
        return Configuration.Builder().setWorkerFactory(factory).build()
    }
}