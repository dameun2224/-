package com.example.cameramap

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class PharmacyClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Pharmacy>
) : DefaultClusterRenderer<Pharmacy>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: Pharmacy, markerOptions: MarkerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions)
        val icon = if (item.day == "일요일") {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }
        markerOptions.icon(icon)
    }
}
