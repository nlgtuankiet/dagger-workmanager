package com.sample.daggerworkmanagersample

import android.app.Application
import androidx.work.Configuration
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject

class SampleApplication : Application(), Configuration.Provider, HasAndroidInjector {

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

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

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }
}