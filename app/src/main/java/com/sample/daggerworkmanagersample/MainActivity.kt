package com.sample.daggerworkmanagersample

import android.app.Activity
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hello_button.setOnClickListener {
            val request = OneTimeWorkRequestBuilder<HelloWorldWorker>().build()
            WorkManager.getInstance().enqueueUniqueWork(
                "Name",
                ExistingWorkPolicy.APPEND,
                request
            )
        }
    }
}
