package com.zenmen.demo.workmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWork(context: Context, parameterName: WorkerParameters) : Worker(context, parameterName) {
    override fun doWork(): Result {
        return Result.success()
    }
}