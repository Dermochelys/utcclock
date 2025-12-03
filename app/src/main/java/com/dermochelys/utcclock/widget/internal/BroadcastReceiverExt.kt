package com.dermochelys.utcclock.widget.internal

import android.content.BroadcastReceiver
import android.util.Log
import com.dermochelys.utcclock.widget.TAG
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Extension to run async work in a BroadcastReceiver using goAsync pattern.
 * Creates a short-lived scope that auto-cancels when work completes.
 */
fun BroadcastReceiver.goAsyncWork(
    coroutineContext: CoroutineContext = Dispatchers.Default,
    block: suspend CoroutineScope.() -> Unit,
) {
    val coroutineScope = CoroutineScope(SupervisorJob() + coroutineContext)
    val pendingResult: BroadcastReceiver.PendingResult? = goAsync()

    coroutineScope.launch {
        try {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Log.e(TAG, "goAsyncWork: BroadcastReceiver execution failed", t)
            } finally {
                coroutineScope.cancel()
            }
        } finally {
            try {
                pendingResult?.finish()
            } catch (e: Exception) {
                Log.e(TAG, "goAsyncWork: Error thrown when trying to finish broadcast", e)
            }
        }
    }
}
