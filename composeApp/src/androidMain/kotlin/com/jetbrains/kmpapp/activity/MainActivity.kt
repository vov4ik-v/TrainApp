package com.jetbrains.kmpapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.jetbrains.kmpapp.R
import com.jetbrains.kmpapp.screens.ListViewModel
import com.jetbrains.kmpapp.screens.SpeedViewModel
import com.jetbrains.kmpapp.service.MapService
import com.jetbrains.kmpapp.service.TrainAnimatorService
import com.jetbrains.kmpapp.ui.UIUpdater
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapService: MapService
    private lateinit var trainAnimator: TrainAnimatorService
    private lateinit var uiUpdater: UIUpdater
    private val speedViewModel: SpeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapService = MapService(supportFragmentManager)
        trainAnimator = TrainAnimatorService()
        mapService.initializeMap(this)

        val statisticButton: Button = findViewById(R.id.statisticsButton)
        statisticButton.setOnClickListener {
            val intent = Intent(this, StatisticActivity::class.java)
            startActivity(intent)
        }

        if (savedInstanceState != null) {
            trainAnimator.setState(savedInstanceState)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapService.setGoogleMap(googleMap)
        val speedTextView = findViewById<TextView>(R.id.speedTextView)
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        uiUpdater = UIUpdater(speedTextView, dateTextView)

        val viewModel = getViewModel<ListViewModel>()
        lifecycleScope.launch {
            viewModel.objects.collect { trainList ->
                mapService.updateMap(trainList)
                trainAnimator.simulateTrainMovement(trainList, mapService, { speed ->
                    speedViewModel.updateSpeed(speed.toFloat())
                }, { speed, date ->
                    uiUpdater.updateTextFields(speed, date)
                })
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        trainAnimator.saveState(outState)
    }
}
