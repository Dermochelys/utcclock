package com.dermochelys.utcclock.view.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

fun Pair<Date, TimeZone>.formatted(simpleDateFormat: SimpleDateFormat): String {
    simpleDateFormat.timeZone = second
    return simpleDateFormat.format(first)
}
