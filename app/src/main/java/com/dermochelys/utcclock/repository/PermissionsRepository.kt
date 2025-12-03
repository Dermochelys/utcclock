package com.dermochelys.utcclock.repository

import kotlinx.coroutines.flow.Flow

interface PermissionsRepository {

    fun canScheduleExactAlarms(): Flow<Boolean>

    fun onExactAlarmPermissionChanged()
}
