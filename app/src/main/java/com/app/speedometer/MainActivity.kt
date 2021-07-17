package com.app.speedometer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.app.speedometer.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
    }

    override fun onResume() {
        super.onResume()
        getLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun getLocationData() {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                print(it)
                val lat = it.latitude
                val lon = it.longitude
                val speed = it.speed

                return@addOnSuccessListener
            }

            val text = "Location data not available"
            val duration = Toast.LENGTH_SHORT

            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()

        }
    }

    private fun getLocationUpdates() {

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                val location = locationResult.lastLocation
//                val lat = location.latitude
//                val lon = location.longitude
                val speed = location.speed
                val altitude = location.altitude
                val altitudeAccuracy = location.verticalAccuracyMeters

                val currentSpeed = ((speed * 60 * 60) / 1000).toInt()
                val speedAccuracy = (location.speedAccuracyMetersPerSecond * 60 * 60) / 1000

                binding.speedometer.setProgress(currentSpeed, true)
                binding.textCurrentSpeed.text = "$currentSpeed"
                binding.textSpeedAccuracy.text = String.format("%.2f", speedAccuracy)
                binding.textAltitude.text = String.format("%.2f", altitude)
                binding.textAltitudeAccuracy.text = String.format("%.2f", altitudeAccuracy)
            }
        }

        fusedLocationClient.requestLocationUpdates(createLocationRequest(), locationCallback, Looper.getMainLooper())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    println("Permission granted")
                } else {
                    println("Permission denied")
                }
                return
            }

            else -> {
                println("")
            }
        }

    }
}