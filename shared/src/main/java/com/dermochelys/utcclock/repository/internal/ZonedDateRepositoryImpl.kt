package com.dermochelys.utcclock.repository.internal

import androidx.annotation.VisibleForTesting
import com.dermochelys.utcclock.repository.ZonedDateRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.TimeZone

class ZonedDateRepositoryImpl @VisibleForTesting internal constructor(private val dispatcher: CoroutineDispatcher,
                                                                      private val dateProvider: DateProvider,
                                                                      private val timeZoneProvider: TimeZoneProvider,
    ): ZonedDateRepository {

    constructor(dispatcher: CoroutineDispatcher = Dispatchers.Default):
            this(dispatcher, DateProviderImpl(dispatcher), TimeZoneProviderImpl(dispatcher))

    private var flow: MutableStateFlow<Pair<Date, TimeZone>>? = null

    override suspend fun zonedDateFlow(): Flow<Pair<Date, TimeZone>> = zonedDateMutableStateFlow()

    override suspend fun onTimeUpdated() {
        // If there is no flow constructed, can simply ignore this call since there is no subscriber
        flow?.emit(getZonedDate())
    }

    // Helpers

    /** Note: Intentionally return a new [Pair] and [Date] here, maintaining immutability, rather
     * than potentially introducing issues with mutating a long-lived [Pair] and/or [Date]. */
    private suspend fun getZonedDate(): Pair<Date, TimeZone> = withContext(dispatcher) {
        Pair(dateProvider.currentDate(), timeZoneProvider.utc())
    }

    private suspend fun zonedDateMutableStateFlow(): MutableStateFlow<Pair<Date, TimeZone>> {
        return withContext(dispatcher) {
            flow?.let { return@withContext it }
            MutableStateFlow(getZonedDate()).also { flow = it }
        }
    }
}

private class DateProviderImpl(private val dispatcher: CoroutineDispatcher): DateProvider {
    override suspend fun currentDate(): Date = withContext(dispatcher) { Date() }
}

private class TimeZoneProviderImpl(private val dispatcher: CoroutineDispatcher): TimeZoneProvider {
    override suspend fun utc(): TimeZone = withContext(dispatcher) { TimeZone.getTimeZone("UTC") }
}

fun interface DateProvider {
    suspend fun currentDate(): Date
}

fun interface TimeZoneProvider {
    suspend fun utc(): TimeZone
}
