package com.example.cameramap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cameramap.databinding.ActivityCameraBinding

class CameraActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent.getStringExtra("owner")?.let { binding.owner.text = it}
        intent.getStringExtra("address")?.let { binding.address.text = it}
        intent.getStringExtra("old")?.let { binding.old.text = it}
        intent.getStringExtra("type")?.let { binding.type.text = it}
        intent.getIntExtra("count", 0).let { binding.count.text = "$it"}
        intent.getIntExtra("resolution", 0)?.let { binding.resolution.text = "$it"}
        intent.getStringExtra("angle")?.let { binding.angle.text = it}
        intent.getIntExtra("archive", 0).let { binding.archive.text = "$it"}
        intent.getStringExtra("installed")?.let { binding.installed.text = it}
        intent.getStringExtra("phone")?.let { binding.phone.text = it}
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        binding.location.text = "$latitude / $longitude"

        binding.close.setOnClickListener {
            this@CameraActivity.finish()
        }
    }
}