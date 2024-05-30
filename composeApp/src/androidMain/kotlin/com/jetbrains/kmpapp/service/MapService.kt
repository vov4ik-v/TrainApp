package com.jetbrains.kmpapp.service

import androidx.fragment.app.FragmentManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.jetbrains.kmpapp.R
import com.jetbrains.kmpapp.data.TrainObject

class MapService(private val fragmentManager: FragmentManager) {
    private var mGoogleMap: GoogleMap? = null
    private var trainMarker: Marker? = null
    private var routePolyline: PolylineOptions? = null
    private var completedRoutePoints: MutableList<LatLng>? = null

    fun initializeMap(callback: OnMapReadyCallback) {
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
    }

    fun setGoogleMap(googleMap: GoogleMap) {
        mGoogleMap = googleMap
    }

    fun updateMap(coordinateList: List<TrainObject>) {
        mGoogleMap?.clear()

        val coordinates = coordinateList.map { LatLng(it.GPSLatitude, it.GPSLongtitude) }
        if (coordinates.isNotEmpty()) {
            routePolyline = PolylineOptions()
                .addAll(coordinates)
                .width(5f)
                .color(0xFF0000FF.toInt())
                .geodesic(true)

            mGoogleMap?.addPolyline(routePolyline!!)

            val startCoordinate = coordinates.first()
            mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(startCoordinate, 13f))

            trainMarker = mGoogleMap?.addMarker(
                MarkerOptions()
                    .position(startCoordinate)
                    .title("Train")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.images))
            )

            completedRoutePoints = mutableListOf()
        }
    }

    fun updateTrainMarkerPosition(newPosition: LatLng) {
        trainMarker?.position = newPosition
        mGoogleMap?.animateCamera(CameraUpdateFactory.newLatLng(newPosition))

        completedRoutePoints?.apply {
            add(newPosition)
            mGoogleMap?.addPolyline(PolylineOptions().addAll(this).width(5f).color(0xFF00FF00.toInt()).geodesic(true))
        }
    }
}
