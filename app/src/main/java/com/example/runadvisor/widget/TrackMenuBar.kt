package com.example.runadvisor.widget
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.runadvisor.R
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*

class TrackMenuBar(
    context: Context?,
    attrs: AttributeSet?):LinearLayout(context,attrs) {
    init{
        inflate(context, R.layout.track_menu_bar,this)
        val height = convertDpToPixel((resources.getDimension(R.dimen.bottomMapMenu)).toInt())
        layoutParams = ViewGroup.LayoutParams(getScreenWidth(),height)
    }

    fun setEventListener(callbackAdd:(args:Any?)->Unit,
                         callbackDecrease:(args:Any?)->Unit,
                         callbackExit:(args:Any?)->Unit){
        val addPointsBtn = (context as HomeActivity).findViewById<CustomImageButton>(R.id.addLassoPoints)
        val decreasePointsBtn = (context as HomeActivity).findViewById<CustomImageButton>(R.id.decreaseLassoPoints)
        val exitLassoPointsBtn = (context as HomeActivity).findViewById<CustomImageButton>(R.id.exitLassoPoints)
        addPointsBtn.setCallback(1,callbackAdd)
        decreasePointsBtn.setCallback(-1,callbackDecrease)
        exitLassoPointsBtn.setCallback(0,callbackExit)
    }

}