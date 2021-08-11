package com.sample.daggerworkmanagersample

import android.content.Context
import android.util.Log
import androidx.work.*
import com.sample.daggerworkmanagersample.HelloWorldWorker.Factory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject
import javax.inject.Provider

class Foo @Inject constructor()

/**
 * @param fooContext assisted parameter can has any name, it better to has same name with parent
 * class parameter and [Factory.create] parameter, this is just for demonstration purpose
 */
class HelloWorldWorker @AssistedInject constructor(
    @Assisted private val fooContext: Context,
    @Assisted private val params: WorkerParameters,
    private val foo: Foo
) : Worker(fooContext, params) {
    private val TAG = "HelloWorldWorker"
    override fun doWork(): Result {
        Log.d(TAG, "Hello world!")
        Log.d(TAG, "Injected foo: $foo")
        return Result.success()
    }

    /**
     * class annotate with @AssistedFactory will available in the dependency graph, you don't need
     * additional binding from [HelloWorldWorker_Factory_Impl] to [Factory].
     *
     * @see: [WorkerBindingModule.bindHelloWorldWorker]
     */
    @AssistedFactory
    interface Factory : ChildWorkerFactory {
        override fun create(appContext: Context, params: WorkerParameters): HelloWorldWorker
    }
}

interface ChildWorkerFactory {
    fun create(appContext: Context, params: WorkerParameters): ListenableWorker
}

/**
 * If there is no worker found, return null to use the default behaviour of [WorkManager]
 * (create worker using refection)
 *
 * Multibinding setup: [WorkerBindingModule]
 *
 * @see WorkerFactory.createWorkerWithDefaultFallback
 */
class SampleWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory>>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry = workerFactories.entries
            .find { Class.forName(workerClassName).isAssignableFrom(it.key) }
            ?: return null
        return foundEntry.value.get().create(appContext, workerParameters)
    }
}

