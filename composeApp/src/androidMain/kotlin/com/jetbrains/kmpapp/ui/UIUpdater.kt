package com.jetbrains.kmpapp.ui

import android.annotation.SuppressLint
import android.widget.TextView

class UIUpdater(private val speedTextView: TextView, private val dateTextView: TextView) {

    @SuppressLint("SetTextI18n")
    fun updateTextFields(speed: Int, date: String) {
        speedTextView.text = "Speed: $speed"
        dateTextView.text = "Date: $date"
    }
}