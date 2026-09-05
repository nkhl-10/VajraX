package com.vajrax.domain.template

import com.vajrax.domain.model.LifePath
import com.vajrax.domain.model.Practice
import com.vajrax.domain.model.Principle
import com.vajrax.domain.model.TrackingMode

data class PathTemplate(
    val path: LifePath,
    val principles: List<Principle>,
    val defaultPractices: List<Practice>,
    val recommendedWakeTime: String,
    val recommendedSleepTime: String,
    val primaryLifeArea: String
)
