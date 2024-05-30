package com.jetbrains.kmpapp.screens
import com.rickclephas.kmm.viewmodel.KMMViewModel
import kotlinx.coroutines.flow.MutableStateFlow


class SpeedViewModel : KMMViewModel() {
    private val _speedData = MutableStateFlow<Float?>(null)

    // Функція для оновлення швидкості
    fun updateSpeed(speed: Float) {
        _speedData.value = speed
    }
}
