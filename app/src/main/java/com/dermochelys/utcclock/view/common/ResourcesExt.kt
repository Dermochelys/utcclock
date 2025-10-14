package com.dermochelys.utcclock.view.common

import android.content.res.Resources
import com.dermochelys.utcclock.R

fun Resources.getRandomTextSizeTime() =
    getRandomDimension(R.dimen.time_font_size_min, R.dimen.time_font_size_max)

fun Resources.getRandomTextSizeDate() =
    getRandomDimension(R.dimen.date_font_size_min, R.dimen.date_font_size_max)

fun Resources.getRandomMiddleSpringWeight() =
    getRandomValue(R.dimen.middle_spring_size_min, R.dimen.middle_spring_size_max)
