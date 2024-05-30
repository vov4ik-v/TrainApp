package com.jetbrains.kmpapp.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.coroutines.cancellation.CancellationException

interface TrainApi {
    suspend fun getData(): List<TrainObject>
}

class KtorTrainApi(private val client: HttpClient) : TrainApi {
    companion object {
        private const val API_URL =
            "https://raw.githubusercontent.com/vov4ik-v/TrainApp/master/TrainData.json"// TODO:Change
    }

    override suspend fun getData(): List<TrainObject> {
        return try {
            client.get(API_URL).body()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            e.printStackTrace()
            emptyList()
        }
    }
}