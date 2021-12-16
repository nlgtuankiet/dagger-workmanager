package com.sample.daggerworkmanagersample

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import javax.inject.Inject

class SampleApplication : Application() {

    private lateinit var sampleComponent: SampleComponent

    @Inject
    lateinit var sampleWorkerFactory: SampleWorkerFactory

    override fun onCreate() {
        sampleComponent = DaggerSampleComponent.create()
        sampleComponent.injectTo(this)
        super.onCreate()
        // use our custom factory so that work manager will use it to create our worker
        val workManagerConfig = Configuration.Builder()
            .setWorkerFactory(sampleWorkerFactory)
            .build()
        WorkManager.initialize(this, workManagerConfig)
    }
}