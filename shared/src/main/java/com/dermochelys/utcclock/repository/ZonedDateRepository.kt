package com.dermochelys.utcclock.repository

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date
import java.util.TimeZone

class ZonedDateRepository() {
    private val zone: TimeZone by lazy { TimeZone.getTimeZone("UTC") }

    val zonedDateFlow: MutableStateFlow<Pair<Date, TimeZone>> by lazy { MutableStateFlow(getZonedDate()) }

    fun onTimeUpdated() { zonedDateFlow.tryEmit(getZonedDate()) }

    /** Note: Intentionally return a new [Pair] and [Date] here, maintaining immutability, rather
     * than potentially introducing issues with mutating a long-lived [Pair] and/or [Date]. */
    private fun getZonedDate(): Pair<Date, TimeZone> = Pair(Date(), zone)
}
