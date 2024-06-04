package com.example.cameramap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cameramap.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.clustering.ClusterManager

private const val TAG = "MainActivity"
private const val LOCATION_PERMISSION_REQUEST_CODE = 1

class MainActivity : AppCompatActivity(), OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<Pharmacy> {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var allPharmacies: List<Pharmacy>
    private lateinit var pharmacies: List<Pharmacy>
    private lateinit var googleMap: GoogleMap
    private lateinit var clusterManager: ClusterManager<Pharmacy>
    private var selectedMarker: Marker? = null

    private var isPharmacyButtonActive = true
    private var isStoreButtonActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val bottomSheet: LinearLayout = findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // 화면의 2/5 크기로 peekHeight 설정
        val displayMetrics = resources.displayMetrics
        val peekHeight = (displayMetrics.heightPixels * 0.4).toInt()
        bottomSheetBehavior.peekHeight = peekHeight
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottomSheetBehavior.peekHeight = peekHeight
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {}
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        bottomSheetBehavior.peekHeight = 60
                    }
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding.pharmacyListView.setOnItemClickListener { _, _, position, _ ->
            val selectedPharmacy = pharmacies[position]
            val selectedCurMarker = getMarkerForPharmacy(selectedPharmacy)

            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(selectedPharmacy.position, 16F),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        selectedCurMarker?.let { // 마커가 있는 경우 - 줌인 되어있는 경우
                            it.showInfoWindow()
                            updateMarkerColor(it, selectedPharmacy)
                            showPharmacyDetails(selectedPharmacy)
                        } ?: run { // 마커가 없는 경우 - 줌인 후에 실행
                            // 지연 후에 마커와 상세 정보를 업데이트
                            Handler(Looper.getMainLooper()).postDelayed({
                                val selectedMarker = getMarkerForPharmacy(selectedPharmacy)

                                selectedMarker?.let {
                                    it.showInfoWindow()
                                    updateMarkerColor(it, selectedPharmacy)
                                }
                                showPharmacyDetails(selectedPharmacy)
                            }, 1000) // 1500ms 지연
                        }
                    }

                    override fun onCancel() {
                        // 애니메이션이 취소된 경우의 동작을 정의할 수 있습니다.
                    }
                }
            )
        }

        // 되돌아가기 버튼 클릭 이벤트 설정
        findViewById<Button>(R.id.backButton).setOnClickListener {
            resetSelectedMarkerColor() // 마커 색상 리셋
            showPharmacyList()
        }

        // 버튼 클릭 이벤트 설정
        findViewById<Button>(R.id.buttonPharmacy).setOnClickListener {
            togglePharmacyButton()
        }
        findViewById<Button>(R.id.buttonStore).setOnClickListener {
            toggleStoreButton()
        }
    }

    private fun togglePharmacyButton() {
        isPharmacyButtonActive = !isPharmacyButtonActive
        updateButtonStates()
        updateMarkersAndList()
    }

    private fun toggleStoreButton() {
        isStoreButtonActive = !isStoreButtonActive
        updateButtonStates()
        updateMarkersAndList()
    }

    private fun updateButtonStates() {
        val pharmacyButton = findViewById<Button>(R.id.buttonPharmacy)
        val storeButton = findViewById<Button>(R.id.buttonStore)

        pharmacyButton.setBackgroundColor(
            if (isPharmacyButtonActive) Color.parseColor("#FFAB91") else Color.parseColor("#FFCCBC")
        )
        storeButton.setBackgroundColor(
            if (isStoreButtonActive) Color.parseColor("#90CAF9") else Color.parseColor("#BBDEFB")
        )
    }

    private fun updateMarkersAndList() {
        clusterManager.clearItems()
        val filteredPharmacies = mutableListOf<Pharmacy>()
        if (isPharmacyButtonActive) {
            filteredPharmacies.addAll(allPharmacies.filter { it.day == "일요일" })
        }
        if (isStoreButtonActive) {
            filteredPharmacies.addAll(allPharmacies.filter { it.day != "일요일" })
        }
        if (isPharmacyButtonActive && isStoreButtonActive) {
            filteredPharmacies.addAll(allPharmacies) // 모든 항목 추가
        }

        filteredPharmacies.forEach { clusterManager.addItem(it) }
        clusterManager.cluster()
        updatePharmacyList(filteredPharmacies)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        clusterManager = ClusterManager(this, googleMap)
        clusterManager.renderer = PharmacyClusterRenderer(this, googleMap, clusterManager)

        clusterManager.setOnClusterItemClickListener(this)
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)

        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(35.8617, 127.1470), 16F
            )
        )

        googleMap.setMinZoomPreference(4F)

        val gson = GsonBuilder()
            .registerTypeAdapter(Pharmacy::class.java, PharmacyDeserializer())
            .create()

        val jsonString = this@MainActivity.assets.open("pharmacy.json").bufferedReader().readText()
        val jsonType = object : TypeToken<PharmacyMap>() {}.type
        val pharmacyMap = gson.fromJson(jsonString, jsonType) as PharmacyMap
        allPharmacies = pharmacyMap.pharmacy

        calculateDistances(getCurrentLatLng(), allPharmacies)
        pharmacies = allPharmacies.sortedBy { it.distance }

        pharmacies.forEach { pharmacy ->
            clusterManager.addItem(pharmacy)
        }

        clusterManager.cluster()
        enableMyLocation(googleMap)
    }

    override fun onClusterItemClick(item: Pharmacy): Boolean {
        val marker = getMarkerForPharmacy(item)
        marker?.let {
            if (it.isVisible) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 16F))
//                updateMarkerColor(marker, item)
                it.showInfoWindow()

                // 리스트 항목 위치로 이동
                val position = pharmacies.indexOf(item)
                binding.pharmacyListView.smoothScrollToPositionFromTop(position, 0)
            }
        }
        return true
    }


    private fun getMarkerForPharmacy(pharmacy: Pharmacy): Marker? {
        return clusterManager.markerCollection.markers.firstOrNull { it.title == pharmacy.name }
    }

    private fun updatePharmacyList(pharmacies: List<Pharmacy>) {
        val sortedPharmacies = pharmacies.sortedBy { it.distance }
        this.pharmacies = sortedPharmacies
        val listItems = sortedPharmacies.map { pharmacy ->
            val day = if (pharmacy.day.isNotEmpty()) pharmacy.day else "정보 없음"
            val number = if (pharmacy.number.isNotEmpty()) pharmacy.number else "정보 없음"
            "${pharmacy.name} (${pharmacy.distance.toInt()} m)\n영업요일: $day\n번호: $number"
        }
        val adapter = CustomArrayAdapter(this, listItems)
        binding.pharmacyListView.adapter = adapter
    }

    private fun updateMarkerColor(marker: Marker?, pharmacy: Pharmacy) {
        selectedMarker?.let {
            if (it.isInfoWindowShown) {
                val originalPharmacy = it.tag as? Pharmacy
                val originalIcon = when (originalPharmacy?.day) {
                    "일요일" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                }
                it.setIcon(originalIcon)
            }
        }

        marker?.let {
            if (it.isVisible) {
                it.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                selectedMarker = marker
            }
        }
    }

    private fun getPharmacyByTitle(title: String?): Pharmacy? {
        return allPharmacies.firstOrNull { it.name == title }
    }

    private fun resetSelectedMarkerColor() {
        selectedMarker?.let {
            val originalPharmacy = getPharmacyByTitle(it.title)
            val originalIcon = when (originalPharmacy?.day) {
                "일요일" -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            }
            it.setIcon(originalIcon)
            selectedMarker = null
        }
    }



    private fun enableMyLocation(googleMap: GoogleMap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            getDeviceLocation(googleMap)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getDeviceLocation(googleMap: GoogleMap) {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16F))
                        calculateDistances(currentLatLng, allPharmacies)
                        updatePharmacyList(allPharmacies)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: ${e.message}")
        }
    }

    private fun calculateDistances(currentLatLng: LatLng, pharmacies: List<Pharmacy>) {
        pharmacies.forEach { pharmacy ->
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLatLng.latitude,
                currentLatLng.longitude,
                pharmacy.latitude,
                pharmacy.longitude,
                results
            )
            pharmacy.distance = results[0]
        }
    }

    private fun showPharmacyDetails(pharmacy: Pharmacy) {
        // Bottom sheet 내용 업데이트
        val pharmacyDetailsLayout = findViewById<LinearLayout>(R.id.pharmacyDetailsLayout)
        pharmacyDetailsLayout.visibility = View.VISIBLE

        val day = if (pharmacy.day.isNotEmpty()) pharmacy.day else "정보 없음"
        val number = if (pharmacy.number.isNotEmpty()) pharmacy.number else "정보 없음"
        val time = if (pharmacy.time.isNotEmpty()) pharmacy.time else "정보 없음"
        val roadNameAddress = if (pharmacy.road_name_address.isNotEmpty()) pharmacy.road_name_address else "정보 없음"
        val localAddress = if (pharmacy.local_address.isNotEmpty()) pharmacy.local_address else "정보 없음"

        findViewById<TextView>(R.id.pharmacyName).text = pharmacy.name
        findViewById<TextView>(R.id.pharmacyDistance).text = "거리: ${pharmacy.distance} m"
        findViewById<TextView>(R.id.pharmacyHours).text = "요일: $day"
        findViewById<TextView>(R.id.pharmacyNumber).text = "전화번호: $number"
        findViewById<TextView>(R.id.pharmacyTime).text = "시간: $time"
        findViewById<TextView>(R.id.pharmacyRoadNameAddress).text = "$roadNameAddress"
        findViewById<TextView>(R.id.pharmacyLocalAddress).text = "$localAddress"

        // 리스트뷰는 숨기고, 되돌아가기 버튼 보이기
        binding.pharmacyListView.visibility = View.GONE
        findViewById<Button>(R.id.backButton).visibility = View.VISIBLE
    }

    private fun showPharmacyList() {
        // Bottom sheet 내용 업데이트
        findViewById<LinearLayout>(R.id.pharmacyDetailsLayout).visibility = View.GONE

        // 리스트뷰는 보이고, 되돌아가기 버튼 숨기기
        binding.pharmacyListView.visibility = View.VISIBLE
        findViewById<Button>(R.id.backButton).visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                mapFragment?.getMapAsync { googleMap ->
                    enableMyLocation(googleMap)
                }
            } else {
                // 권한이 거부된 경우 오류 메시지 표시
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getCurrentLatLng(): LatLng {
        var currentLatLng = LatLng(35.8617, 127.1470) // Default location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLatLng = LatLng(location.latitude, location.longitude)
                }
            }
        }
        return currentLatLng
    }
}
