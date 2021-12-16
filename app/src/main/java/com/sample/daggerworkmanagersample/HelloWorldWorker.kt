package com.sample.daggerworkmanagersample

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sample.daggerworkmanagersample.HelloWorldWorker.Factory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/**
 * @param fooContext assisted parameter can has any name, it better to has same name with parent
 * class parameter and [Factory.create] parameter, this is just for demonstration purpose
 */
class HelloWorldWorker @AssistedInject constructor(
    @Assisted private val fooContext: Context,
    @Assisted private val params: WorkerParameters,
    private val foo: Foo,
) : Worker(fooContext, params) {
    private val tag = "HelloWorldWorker"
    override fun doWork(): Result {
        Log.d(tag, "Hello world!")
        Log.d(tag, "Injected foo: $foo")
        return Result.success()
    }

    /**
     * class annotate with @AssistedFactory will available in the dependency graph, you don't need
     * additional binding from [HelloWorldWorker_Factory_Impl] to [Factory].
     */
    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): HelloWorldWorker
    }
}



