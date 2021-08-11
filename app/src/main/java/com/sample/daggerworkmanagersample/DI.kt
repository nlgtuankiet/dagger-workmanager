package com.sample.daggerworkmanagersample

import androidx.work.ListenableWorker
import dagger.Binds
import dagger.Component
import dagger.MapKey
import dagger.Module
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Component(
    modules = [
        AndroidInjectionModule::class,
        DaggerAndroidWorker.Module::class,
        WorkerBindingModule::class
    ]
)
interface SampleComponent : AndroidInjector<SampleApplication> {

    fun factory(): SampleWorkerFactory
}

@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)

@Module
interface WorkerBindingModule {
    @Binds
    @IntoMap
    @WorkerKey(HelloWorldWorker::class)
    fun bindHelloWorldWorker(factory: HelloWorldWorker.Factory): ChildWorkerFactory
}


