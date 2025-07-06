package com.dermochelys.utcclock

import android.content.Context
import android.content.pm.PackageManager

fun Context.isRunningOnTv() = packageManager.isRunningOnTv()

fun PackageManager.isRunningOnTv() = hasSystemFeature(PackageManager.FEATURE_LEANBACK)