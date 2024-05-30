package com.jetbrains.kmpapp.service

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.model.LatLng
import com.jetbrains.kmpapp.data.TrainObject

class TrainAnimatorService {

    private var currentIndex = 0
    private var isMoving = false
    private val delayMillis = 1000L
    private val handler = Handler(Looper.getMainLooper())

    fun simulateTrainMovement(
        trainList: List<TrainObject>,
        mapService: MapService,
        updateSpeed: (Int) -> Unit,
        onUpdateUI: (Int, String) -> Unit
    ) {
        if (!isMoving) {
            isMoving = true
            handler.postDelayed({
                moveTrain(trainList, mapService, updateSpeed, onUpdateUI)
            }, delayMillis / 15)
        }
    }

    private fun moveTrain(
        trainList: List<TrainObject>,
        mapService: MapService,
        updateSpeed: (Int) -> Unit,
        onUpdateUI: (Int, String) -> Unit
    ) {
        if (currentIndex < trainList.size - 1) {
            currentIndex++
            val newPosition = LatLng(
                trainList[currentIndex].GPSLatitude,
                trainList[currentIndex].GPSLongtitude
            )
            mapService.updateTrainMarkerPosition(newPosition)
            onUpdateUI(trainList[currentIndex].TrainSpeed, trainList[currentIndex].dDate)
            updateSpeed(trainList[currentIndex].TrainSpeed)
            handler.postDelayed({
                moveTrain(trainList, mapService, updateSpeed, onUpdateUI)
            }, delayMillis / 15)
        } else {
            isMoving = false
        }
    }

    fun setState(savedInstanceState: Bundle) {
        currentIndex = savedInstanceState.getInt("currentIndex", 0)
        isMoving = savedInstanceState.getBoolean("isMoving", false)
    }

    fun saveState(outState: Bundle) {
        outState.putInt("currentIndex", currentIndex)
        outState.putBoolean("isMoving", isMoving)
    }
}
