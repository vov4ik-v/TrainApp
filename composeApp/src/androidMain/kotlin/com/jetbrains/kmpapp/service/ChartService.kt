package com.jetbrains.kmpapp.service

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class ChartService {
    private var lineChart: LineChart? = null

    fun initialize(chart: LineChart) {
        lineChart = chart
        setupLineChart()
    }

    fun addEntry(speed: Float) {
        lineChart?.let { chart ->
            val data = chart.data
            val set = data.getDataSetByIndex(0)

            data.addEntry(Entry(set?.entryCount?.toFloat() ?: 0f, speed), 0)

            data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    }

    private fun setupLineChart() {
        lineChart?.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.BOTTOM

            val yAxis = axisLeft
            yAxis.setDrawGridLines(false)
            yAxis.setDrawLabels(true)

            axisRight.isEnabled = false

            val line = LineDataSet(null, "Speed").apply {
                color = ColorTemplate.COLORFUL_COLORS[0]
                setCircleColor(ColorTemplate.COLORFUL_COLORS[0])
                setDrawValues(false)
            }

            val data = LineData(line)
            this.data = data
        }
    }
}
