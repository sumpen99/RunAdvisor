package com.example.runadvisor.background
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import com.example.runadvisor.MainActivity
import com.example.runadvisor.io.printToTerminal


class LocationService(val parentActivity:MainActivity) : Service() {
    private var mLocationManager: LocationManager? = null
    private val LOCATION_INTERVAL = 5000
    private val LOCATION_DISTANCE = 5f

    var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))

    inner class LocationListener(provider: String) : android.location.LocationListener {
        private var mLastLocation: Location

        init {
            printToTerminal("LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            printToTerminal("onLocationChanged: $location")
            mLastLocation.set(location)
            printToTerminal("LastLocation ${mLastLocation.latitude.toString()} ${mLastLocation.longitude.toString()}")
        }

        override fun onProviderDisabled(provider: String) {
            printToTerminal("onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            printToTerminal("onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            printToTerminal("onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        printToTerminal("onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        printToTerminal("onCreate")
        initializeLocationManager()
        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[1])
        } catch (err: java.lang.SecurityException) {
            printToTerminal("fail to request location update, ignore ${err.message}")
        } catch (err: IllegalArgumentException) {
            printToTerminal("network provider does not exist, ${err.message}")
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[0])
        } catch (err: java.lang.SecurityException) {
            printToTerminal("fail to request location update, ignore ${err.message}")
        } catch (err: IllegalArgumentException) {
            printToTerminal("gps provider does not exist ${err.message}")
        }

    }

    override fun onDestroy() {
        printToTerminal("onDestroy LocationService")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (err: Exception) {
                    printToTerminal("fail to remove location listners, ignore ${err.message}")
                }

            }
        }
    }

    private fun initializeLocationManager() {
        printToTerminal("initializeLocationManager")
        if (mLocationManager == null) {

            mLocationManager =  parentActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

}