package com.dermochelys.utcclock.repository.internal

import android.content.Context
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.widget.canScheduleExactAlarms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PermissionsRepositoryImpl(
    private val context: Context,
) : PermissionsRepository {

    private val canScheduleExactAlarmsFlow = MutableStateFlow(canScheduleExactAlarms(context))

    override fun canScheduleExactAlarms(): Flow<Boolean> = canScheduleExactAlarmsFlow

    override fun onExactAlarmPermissionChanged() {
        canScheduleExactAlarmsFlow.value = canScheduleExactAlarms(context)
    }
}
