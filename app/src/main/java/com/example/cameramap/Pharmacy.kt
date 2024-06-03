package com.example.cameramap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class Pharmacy(
    val name: String,
    val number: String,
    val road_name_address: String,
    val local_address: String,
    val latitude: Double,
    val longitude: Double,
    val day: String,
    val time: String,
    val week: String
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun getTitle(): String? {
        return name
    }

    override fun getSnippet(): String? {
        return "$number"
    }
}