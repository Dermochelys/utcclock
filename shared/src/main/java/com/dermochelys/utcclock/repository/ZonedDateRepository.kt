package com.dermochelys.utcclock.repository

import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.TimeZone

interface ZonedDateRepository {
    suspend fun zonedDateFlow(): Flow<Pair<Date, TimeZone>>

    suspend fun onTimeUpdated()
}
