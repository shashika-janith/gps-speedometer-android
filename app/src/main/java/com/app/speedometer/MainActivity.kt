package com.app.speedometer

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.speedometer.databinding.ActivityMainBinding
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        init()
    }

    override fun onResume() {
        super.onResume()
//        requestLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun init() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return

                val location = locationResult.lastLocation

                if (location != null) {
                    println("$location.latitude, ${location.longitude}")

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
                    binding.textLatitude.text = "Lat: ${location.latitude}"
                    binding.textLongitude.text = "Long: ${location.longitude}"

                    binding.textDistanceTravelled.text = String.format("%.2f", lastLocation.distanceTo(location))
                    lastLocation = location
                }
            }
        }

        getLastLocation()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun requestLocationPermission() {
        val locationPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                when {
                    it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
//                        getLocationUpdates()
                        getLastLocation()
                        Toast.makeText(this, "Fine location access granted.", Toast.LENGTH_SHORT).show()
                    }
                    it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        // TODO: Show user a dialog and close the application.
                        Toast.makeText(this, "Only approximate location access granted.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        // No location access granted.
                        // TODO: Show user a dialog and close the application.
                        Toast.makeText(this, "No location access granted.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    lastLocation = location
                    getLocationUpdates()
                } else {
                    Toast.makeText(this, "Last location is unavailable.", Toast.LENGTH_SHORT).show()
                }
            }
    }

//    private fun getLocationData() {
//        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
//            return
//        }
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//            location?.let {
//                print(it)
//                val lat = it.latitude
//                val lon = it.longitude
//                val speed = it.speed
//
//                return@addOnSuccessListener
//            }
//
//            val text = "Location data not available"
//            val duration = Toast.LENGTH_SHORT
//
//            val toast = Toast.makeText(applicationContext, text, duration)
//            toast.show()
//
//        }
//    }

    private fun getLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(
            createLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

//    private fun startLocationUpdates() {
//        fusedLocationClient.requestLocationUpdates(
//            createLocationRequest(),
//            locationCallback,
//            Looper.getMainLooper()
//        )
//    }

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