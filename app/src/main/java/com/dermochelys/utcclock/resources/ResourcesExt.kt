package com.dermochelys.utcclock.resources

import android.content.res.Resources
import com.dermochelys.utcclock.getRandomDimension
import com.dermochelys.utcclock.getRandomValue
import com.dermochelys.utcclock.shared.R

internal fun Resources.getRandomTextSizeTime() =
    getRandomDimension(R.dimen.time_font_size_min, R.dimen.time_font_size_max)

internal fun Resources.getRandomTextSizeDate() =
    getRandomDimension(R.dimen.date_font_size_min, R.dimen.date_font_size_max)

internal fun Resources.getRandomMiddleSpringWeight() =
    getRandomValue(R.dimen.middle_spring_size_min, R.dimen.middle_spring_size_max)
