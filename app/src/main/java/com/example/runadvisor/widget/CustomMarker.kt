package com.example.runadvisor.widget
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.example.runadvisor.io.printToTerminal
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.OverlayItem

class CustomMarker(title:String,snippet:String,geoPoint:GeoPoint):
    OverlayItem(title,snippet,geoPoint), View.OnTouchListener{

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        printToTerminal("hepp")
        return true
    }


}