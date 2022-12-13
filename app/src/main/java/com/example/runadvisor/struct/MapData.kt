package com.example.runadvisor.struct

import android.graphics.Bitmap
import org.osmdroid.util.GeoPoint

class MapData {
    var zoom:Int = 0
    var geoPoint:GeoPoint? = null
    var coordinates:String=""
    var screenshot:Bitmap? = null
}