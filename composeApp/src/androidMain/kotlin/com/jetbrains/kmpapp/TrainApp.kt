package com.jetbrains.kmpapp

import android.app.Application
import com.jetbrains.kmpapp.di.initKoin
import com.jetbrains.kmpapp.screens.ListViewModel
import com.jetbrains.kmpapp.screens.SpeedViewModel
import org.koin.dsl.module

class TrainApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            listOf(
                module {
                    factory { ListViewModel(get()) }
                    factory { SpeedViewModel() }
                }
            )
        )
    }
}
