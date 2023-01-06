package com.example.runadvisor.widget
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class GpsBlinker(context:Context,
                 attrs: AttributeSet?=null,
                 defStyleAttr:Int=0):LinearLayout(context, attrs,defStyleAttr){
    private lateinit var imgView:ImageView
    private lateinit var frameAnimation:AnimationDrawable
    private var callbackUpdateLength:(args:String)->Unit = ::templateFunctionString
    private var geoPoints = ArrayList<GeoPoint>()
    private var lastGeoPoint:GeoPoint? = null
    private var measuredLength:Double = 0.0
    private var active:Boolean = false
    private var storePoints:Boolean = false
    private val offsetY:Int = convertDpToPixel(25)
    private val offsetX:Int = convertDpToPixel(25)

    init{
        inflate(context, R.layout.gps_blinker,this)
    }


    fun setCallbackUpdateLength(callback:(args:String)->Unit){
        callbackUpdateLength = callback
    }

    fun shouldStorePoints(value:Boolean){
        storePoints = value
    }

    fun clearCollectedPoints(){
        measuredLength = 0.0
        geoPoints.clear()
    }

    fun refreshMeasuredLength(){
        callbackUpdateLength(measuredLength.inKilometers())
    }

    fun resetAndClear(){
        clearCollectedPoints()
        refreshMeasuredLength()
        storePoints = false
    }

    fun getGeoPoints():List<GeoPoint>{
        return geoPoints
    }

    fun getMeasuredLength():Double{
        return measuredLength
    }

    fun collectPoints(geoPoint:GeoPoint){
        lastGeoPoint = geoPoint
        if(storePoints){
            if(geoPoints.size > 0){
                measuredLength+= latLonToMeter(geoPoint,geoPoints[geoPoints.size-1])
                callbackUpdateLength(measuredLength.inKilometers())
            }
            if(measuredLength <= MAX_STORAGE_LENGTH){geoPoints.add(geoPoint)}
        }
    }

    fun resetPosition(mapView:MapView){
        if(lastGeoPoint!=null){
            val p = mapView.projection.toPixels(lastGeoPoint,null)
            setPosition(p.x.toFloat(),p.y.toFloat())
        }
    }
    fun setPosition(x:Float,y:Float){
        this.x = x-offsetX
        this.y = y-offsetY
    }

    fun setAnimation(){
        imgView = (context as MainActivity).findViewById(R.id.gpsBlinkerImageView)
        frameAnimation = imgView.drawable as AnimationDrawable
    }

    fun startBlinking(){
        active = true
        visibility = VISIBLE
        frameAnimation.start()
    }

    fun stopBlinking(){
        active = false
        visibility = GONE
        frameAnimation.stop()
    }

    fun isNotActive():Boolean{
        return !active
    }

    fun isActive():Boolean{
        return active
    }

    fun isCollecting():Boolean{
        return storePoints
    }
}