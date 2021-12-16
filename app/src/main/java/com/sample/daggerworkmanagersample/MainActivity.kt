package com.sample.daggerworkmanagersample

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.hello_button).setOnClickListener {
            WorkManager.getInstance(this).enqueue(
                OneTimeWorkRequestBuilder<HelloWorldWorker>().build()
            )
        }
    }
}
