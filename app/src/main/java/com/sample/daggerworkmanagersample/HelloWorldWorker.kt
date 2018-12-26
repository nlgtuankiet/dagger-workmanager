package com.sample.daggerworkmanagersample

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import javax.inject.Inject
import javax.inject.Provider

class Foo @Inject constructor()

class HelloWorldWorker @AssistedInject constructor(
    @Assisted private val params: WorkerParameters,
    private val appContext: Context,
    private val foo: Foo
) : Worker(appContext, params) {
    private val TAG = "HelloWorldWorker"
    override fun doWork(): Result {
        Log.d(TAG, "Hello world!")
        Log.d(TAG, "Injected foo: $foo")
        return Result.success()
    }

    @AssistedInject.Factory
    interface Factory  : ChildWorkerFactory<HelloWorldWorker>
}

interface ChildWorkerFactory<T : ListenableWorker> {
    fun create(params: WorkerParameters): T
}

class SampleWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards Provider<ChildWorkerFactory<out ListenableWorker>>>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val foundEntry =
            workerFactories.entries.find { Class.forName(workerClassName).isAssignableFrom(it.key) }
        val factory = foundEntry?.value
            ?: throw IllegalArgumentException("unknown worker class name: $workerClassName")
        return factory.get().create(workerParameters)
    }
}

