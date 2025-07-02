package com.dermochelys.utcclock.repository

import kotlinx.coroutines.flow.Flow

interface DisclaimerRepository {

    suspend fun onDisclaimerAgreeClicked()

    fun shouldShowDisclaimer(): Flow<Boolean>
}
