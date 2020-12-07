package dev.rostopira.geofencehmsbug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeofenceEventsReceiver: BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        Log.wtf("Geofence", "This never called")
    }

}