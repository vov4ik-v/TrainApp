package com.jetbrains.kmpapp.activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.jetbrains.kmpapp.R
import com.jetbrains.kmpapp.service.ChartService


class StatisticActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        val lineChart = findViewById<LineChart>(R.id.speedChart)
        val chartService = ChartService()
        chartService.initialize(lineChart)

        chartService.addEntry(0.0f)
        chartService.addEntry(10.0f)
    }
}
