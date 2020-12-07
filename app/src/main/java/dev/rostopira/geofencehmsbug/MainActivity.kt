package dev.rostopira.geofencehmsbug

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.huawei.hms.location.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
        else
            startUpdatingLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startUpdatingLocation()
    }

    private fun startUpdatingLocation() {
        val locMan = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(0f)
            .setInterval(10L)
            .setExpirationDuration(-1)

        locMan.requestLocationUpdates(locationRequest, object: LocationCallback() {
            override fun onLocationResult(lr: LocationResult) {
                registerGeofence(lr.lastLocation)
            }
        }, mainLooper)
    }

    private fun registerGeofence(location: Location) {
        val intent = Intent(this, GeofenceEventsReceiver::class.java).setAction(BROADCAST)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val geofence = Geofence.Builder()
            .setUniqueId("geofence")
            .setRoundArea(location.latitude, location.longitude, 500f)
            .setValidContinueTime(Geofence.GEOFENCE_NEVER_EXPIRE)
            .setConversions(Geofence.EXIT_GEOFENCE_CONVERSION)
            .build()
        val geofenceRequest = GeofenceRequest.Builder()
            .setInitConversions(GeofenceRequest.ENTER_INIT_CONVERSION or GeofenceRequest.DWELL_INIT_CONVERSION)
            .createGeofence(geofence)
            .build()
        LocationServices.getGeofenceService(this).createGeofenceList(geofenceRequest, pendingIntent)
            .addOnSuccessListener {
                Log.w("Geofence", "Registered geofence")
            }
            .addOnFailureListener {
                Log.wtf("Geofence", it)
            }
    }

    companion object {
        const val BROADCAST = "dev.rostopira.geofencehmsbug.GeofenceEventsReceiver"
    }

}