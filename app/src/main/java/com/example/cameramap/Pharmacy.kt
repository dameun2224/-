package com.example.cameramap

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterItem

data class Pharmacy(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val day: String,
    val number: String,
    val time: String,
    val road_name_address: String,
    val local_address: String,
    var distance: Float = 0.0f
) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(latitude, longitude)
    override fun getTitle(): String = name
    override fun getSnippet(): String = number
}
