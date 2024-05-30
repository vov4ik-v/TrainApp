package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.InMemoryMuseumStorage
import com.jetbrains.kmpapp.data.KtorTrainApi
import com.jetbrains.kmpapp.data.TrainApi
import com.jetbrains.kmpapp.data.TrainRepository
import com.jetbrains.kmpapp.data.TrainStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
        }
    }

    single<TrainApi> { KtorTrainApi(get()) }
    single<TrainStorage> { InMemoryMuseumStorage() }
    single {
        TrainRepository(get(), get()).apply {
            initialize()
        }
    }
}


fun initKoin() = initKoin(emptyList())

fun initKoin(extraModules: List<Module>) {
    startKoin {
        modules(
            dataModule,
            *extraModules.toTypedArray(),
        )
    }
}
