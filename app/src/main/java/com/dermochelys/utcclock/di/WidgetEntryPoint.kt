package com.dermochelys.utcclock.di

import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Entry point for accessing dependencies needed by GlanceAppWidget.
 * This allows the widget to retrieve repositories from Hilt's dependency graph.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun disclaimerRepository(): DisclaimerRepository
    fun permissionsRepository(): PermissionsRepository
    fun zonedDateRepository(): ZonedDateRepository
}