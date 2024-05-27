package com.jetbrains.kmpapp.data

import kotlinx.serialization.Serializable

@Serializable
data class TrainObject(
    val dDate: String,
    val GPSLatitude: Double,
    val GPSLongtitude: Double,
    val TrainSpeed: Int,
    val id:Int
)
