package com.example.runadvisor.methods
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import java.lang.Math.pow
import kotlin.math.*

fun toRadians(degress:Double):Double{
    return degress * (Math.PI / 180);
}

fun toDegrees(radians:Double):Double{
    return radians * (180 / Math.PI)
}

fun calculateTrackLength(p:ArrayList<GeoPoint>):Double{
    var i = 1
    var l = 0.0
    while(i<p.size){
        l+= latLonToMeter(p[i-1],p[i])
        i++
    }
    return l
}

fun getGeoPointToDouble(p:GeoPoint):ArrayList<Double>{
    val newList = ArrayList<Double>()
    newList.add(p.latitude)
    newList.add(p.longitude)
    return newList
}

fun getGeoPointsToDouble(p:ArrayList<GeoPoint>):ArrayList<Double>{
    val newList = ArrayList<Double>()
    var i = 0
    while(i<p.size){
        newList.add(p[i].latitude)
        newList.add(p[i].longitude)
        i++
    }
    return newList
}

fun getDoubleToGeoPoints(p:ArrayList<Double>):ArrayList<GeoPoint>{
    val newList = ArrayList<GeoPoint>()
    var i = 1
    while(i<p.size){
        newList.add(GeoPoint(p[i-1],p[i]))
        i+=2
    }
    return newList
}

fun getDoubleToGeoPoint(p:ArrayList<Double>):GeoPoint{
    return GeoPoint(p[0],p[1])
}

fun getCenterOfPoints(p:ArrayList<GeoPoint>):GeoPoint?{
    if(p.isEmpty()){return null}
    var i = 1
    var minLat = p[0].latitude
    var maxLat = p[0].latitude
    var minLon = p[0].longitude
    var maxLon = p[0].longitude
    while(i<p.size){
        minLat = Math.min(minLat,p[i].latitude)
        maxLat = Math.max(maxLat,p[i].latitude)
        minLon = Math.min(minLon,p[i].longitude)
        maxLon = Math.max(maxLon,p[i].longitude)
        i++
    }
    return GeoPoint(getMiddlePoint(minLat,maxLat),getMiddlePoint(minLon,maxLon))
}

fun getMiddlePoint(minPoint: Double, maxPoint: Double):Double{
    return minPoint + (maxPoint - minPoint) / 2
}

fun latLonToMeter(p1:GeoPoint,p2:GeoPoint):Double{
    val r = 6371e3
    val d1 = toRadians(p1.latitude)
    val d2 = toRadians(p2.latitude)
    val z1 = p1.altitude
    val z2 = p2.altitude
    val dlat = toRadians(p2.latitude-p1.latitude)
    val dlon = toRadians(p2.longitude-p1.longitude)

    val a = (sin(dlat/2) * sin(dlat/2) +
            cos(d1) * cos(d2) *
            sin(dlon/2) * sin(dlon/2))
    val c = 2 * atan2(sqrt(a),sqrt(1-a))

    val d = pow(r*c,2.0)
    val e = pow(abs(z1-z2),2.0)
    //return r*c
    return sqrt(d+e)
}



fun getGeoMiddle(p1:GeoPoint,p2:GeoPoint):GeoPoint{
    val dlon = toRadians(p2.longitude-p1.longitude)

    val lat1 = toRadians(p1.latitude)
    val lat2 = toRadians(p2.latitude)
    val lon1 = toRadians(p1.longitude)

    val bx = cos(lat2) * cos(dlon)
    val by = cos(lat2) * sin(dlon)
    val clat = Math.toDegrees(atan2(
        sin(lat1) + sin(lat2),
        sqrt((cos(lat1) + bx) * (cos(lat1) + bx) + by * by)
    ))
    val clon = toDegrees(lon1 + atan2(by, cos(lat1) + bx))
    return GeoPoint(clat,clon)
}

fun getAreaGeoMiddle(bbox:BoundingBox):GeoPoint{
    val ry1: Double = Math.log((Math.sin(toRadians(bbox.latSouth)) + 1) / Math.cos(toRadians(bbox.latSouth)))
    val ry2: Double = Math.log(((Math.sin(toRadians(bbox.latNorth)) + 1) / Math.cos(toRadians(bbox.latNorth))))

    val ryc = (ry1 + ry2) / 2

    val centerY: Double = toDegrees(Math.atan(Math.sinh(ryc)))

    //val resolutionHorizontal: Double = (bbox.lonWest/bbox.lonEast) /  getScreenWidth()

    //val vy0: Double = Math.log(Math.tan(Math.PI * (0.25 + centerY / 360)))
    //val vy1: Double = Math.log(Math.tan(Math.PI * (0.25 + bbox.latNorth / 360)))
    //val viewHeightHalf: Double = getScreenHeight() / 2.0
    //val zoomFactorPowered = (viewHeightHalf / (40.7436654315252 * (vy1 - vy0)))
    //val resolutionVertical = 360.0 / (zoomFactorPowered * DP_TILE_SIZE)

    //val resolution: Double = (Math.max(resolutionHorizontal, resolutionVertical) * paddingFactor)
    //val zoom: Double = Math.log(360 / (resolution * DP_TILE_SIZE))
    //minPoint + (maxPoint - minPoint) / 2
    //val lon: Double = (bbox.lonEast + ((bbox.lonWest-bbox.lonEast)/2))
    val lon = getMiddlePoint(bbox.lonEast,bbox.lonWest)
    val lat = centerY
    return GeoPoint(lat,lon)
}

fun getAreaZoomLevel(bbox:BoundingBox):Double{
    val latFraction = (latRad(bbox.latNorth) - latRad(bbox.latNorth)) / Math.PI
    val lngDiff = bbox.lonEast - bbox.lonWest
    val lngFraction = (if (lngDiff < 0) lngDiff + 360 else lngDiff) / 360
    val latZoom = zoom(getScreenHeight().toDouble(), DP_TILE_SIZE, latFraction)
    val lngZoom = zoom(getScreenWidth().toDouble(), DP_TILE_SIZE, lngFraction)
    val zoom = Math.min(Math.min(latZoom, lngZoom), 20.0)
    return zoom
}

private fun latRad(lat: Double): Double {
    val sin = Math.sin(lat * Math.PI / 180)
    val radX2 = Math.log((1 + sin) / (1 - sin)) / 2
    return Math.max(Math.min(radX2, Math.PI), -Math.PI) / 2
}

private fun zoom(mapPx: Double, worldPx: Double, fraction: Double): Double {
    return Math.log(mapPx / worldPx / fraction) / LN2
}

/*
fun clamp(x:Double,minimum:Double,maximum:Double):Double{
    return Math.max(minimum,Math.min(x,maximum))
}

fun getX(location:GeoPoint):Double{
    val zoom = mapView.projection.zoomLevel
    val lon = clamp(location.longitude,MIN_LONGITUDE,MAX_LONGITUDE)
    return ((lon + 180.0) / 360.0 * Math.pow(2.0, zoom)) * DP_TILE_SIZE
}

fun getY(location:GeoPoint):Double{
    val zoom = mapView.projection.zoomLevel
    var lat = clamp(-location.latitude, MIN_LATITUDE, MAX_LATITUDE)
    lat = lat * Math.PI/ 180.0

    return ((1.0 - Math.log(Math.tan(lat) + 1.0 / Math.cos(lat)) / Math.PI) /
            2.0 * Math.pow(2.0, zoom)) * DP_TILE_SIZE
}*/


