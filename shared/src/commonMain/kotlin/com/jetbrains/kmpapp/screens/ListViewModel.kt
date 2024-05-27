package com.jetbrains.kmpapp.screens

import com.jetbrains.kmpapp.data.TrainObject
import com.jetbrains.kmpapp.data.TrainRepository
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

class ListViewModel(trainRepository: TrainRepository) : KMMViewModel() {
    @NativeCoroutinesState
    val objects: StateFlow<List<TrainObject>> =
        trainRepository.getObjects()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
