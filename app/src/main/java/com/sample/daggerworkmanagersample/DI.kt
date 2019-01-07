package com.sample.daggerworkmanagersample

import android.content.Context
import androidx.work.ListenableWorker
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Component(
    modules = [
        SampleAssistedInjectModule::class,
        WorkerBindingModule::class
    ]
)
interface SampleComponent {
    fun factory(): SampleWorkerFactory
}

@Module(includes = [AssistedInject_SampleAssistedInjectModule::class])
@AssistedModule
interface SampleAssistedInjectModule

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


