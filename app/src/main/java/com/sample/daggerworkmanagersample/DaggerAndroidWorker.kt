package com.sample.daggerworkmanagersample

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.android.ContributesAndroidInjector
import dagger.android.HasAndroidInjector
import javax.inject.Inject


object ContextInjection {
    @JvmStatic
    fun inject(to: Any, with: Context) {
        (with.applicationContext as HasAndroidInjector).androidInjector().inject(to)
    }
}

class DaggerAndroidWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    @Inject
    lateinit var foo: Foo

    init {
        ContextInjection.inject(to = this, with = context)
    }

    override fun doWork(): Result {
        Log.d("DaggerAndroidWorker", "Injected foo: $foo")
        return Result.success()
    }


    @dagger.Module
    interface Module {
        @ContributesAndroidInjector
        fun worker(): DaggerAndroidWorker
    }
}