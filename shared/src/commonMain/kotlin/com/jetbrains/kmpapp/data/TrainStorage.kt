package com.jetbrains.kmpapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

interface TrainStorage {
    suspend fun saveObjects(newObjects: List<TrainObject>)

    fun getObjectById(objectId: Int): Flow<TrainObject?>

    fun getObjects(): Flow<List<TrainObject>>
}

class InMemoryMuseumStorage : TrainStorage {
    private val storedObjects = MutableStateFlow(emptyList<TrainObject>())

    override suspend fun saveObjects(newObjects: List<TrainObject>) {
        storedObjects.value = newObjects
    }

    override fun getObjectById(objectId: Int): Flow<TrainObject?> {
        return storedObjects.map { objects ->
            objects.find { it.id == objectId }
        }
    }

    override fun getObjects(): Flow<List<TrainObject>> = storedObjects
}
