package com.vajrax.android.account

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vajrax.domain.sync.CloudSync
import com.vajrax.domain.sync.SyncStatus
import org.koin.core.context.GlobalContext
import java.util.concurrent.TimeUnit

/**
 * Background sync while signed in: hourly, and shortly after a change made outside the app
 * (widget, notification). Both wait for a network connection; the app itself also syncs while open.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val sync = GlobalContext.getOrNull()?.getOrNull<CloudSync>() ?: return Result.success()
        sync.start()
        return when (sync.status.value) {
            is SyncStatus.Waiting -> Result.retry()
            else -> Result.success()
        }
    }
}

object SyncWork {
    private const val PERIODIC = "vajrax-sync-periodic"
    private const val SOON = "vajrax-sync-soon"
    private const val PERIOD_HOURS = 1L
    private const val SOON_DELAY_SECONDS = 10L
    private const val BACKOFF_SECONDS = 30L

    private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIOD_HOURS, TimeUnit.HOURS)
            .setConstraints(online)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun requestSoon(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(online)
            .setInitialDelay(SOON_DELAY_SECONDS, TimeUnit.SECONDS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(SOON, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).run {
            cancelUniqueWork(PERIODIC)
            cancelUniqueWork(SOON)
        }
    }
}
