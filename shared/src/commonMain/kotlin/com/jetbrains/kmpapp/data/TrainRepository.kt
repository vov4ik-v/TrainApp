package com.jetbrains.kmpapp.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TrainRepository(
    private val trainApi: TrainApi,
    private val trainStorage: TrainStorage,
    ) {
    private val scope = CoroutineScope(SupervisorJob())

    fun initialize() {
        scope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        trainStorage.saveObjects(trainApi.getData())
    }

    fun getObjects(): Flow<List<TrainObject>> = trainStorage.getObjects()

    fun getObjectById(objectId: Int): Flow<TrainObject?> = trainStorage.getObjectById(objectId)
}
