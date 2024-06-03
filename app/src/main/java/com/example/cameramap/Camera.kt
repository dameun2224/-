package com.example.cameramap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class Camera(
    val address: String,
    val angle: String,
    val archive: Int,
    val count: Int,
    val installed: String,
    val latitude: Double,
    val longitude: Double,
    val no: Int,
    val old: String,
    val owner: String,
    val phone: String,
    val resolution: Int,
    val type: String,
    val updated: String
) : ClusterItem {
    override fun getPosition(): LatLng {
        return LatLng(latitude, longitude)
    }

    override fun getTitle(): String? {
        return type
    }

    override fun getSnippet(): String? {
        return "$latitude, $longitude"
    }
}