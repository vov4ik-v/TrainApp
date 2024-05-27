package com.jetbrains.kmpapp

import com.jetbrains.kmpapp.data.TrainRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class KoinDependencies : KoinComponent {
    val museumRepository: TrainRepository by inject()
}
