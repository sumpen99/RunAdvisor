package com.example.runadvisor.widget
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.convertDpToPixel
import org.osmdroid.util.GeoPoint

class GpsBlinker(context:Context,
                 attrs: AttributeSet?=null,
                 defStyleAttr:Int=0):LinearLayout(context, attrs,defStyleAttr){
    private lateinit var imgView:ImageView
    private lateinit var frameAnimation:AnimationDrawable
    private var active:Boolean = false
    init{
        inflate(context, R.layout.gps_blinker,this)
    }

    fun setPosition(x:Float,y:Float){
        this.x = x
        this.y = y
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
}