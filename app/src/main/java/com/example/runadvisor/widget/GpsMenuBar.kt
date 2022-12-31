package com.example.runadvisor.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.convertDpToPixel
import com.example.runadvisor.methods.getScreenWidth

class GpsMenuBar(
    context: Context?,
    attrs: AttributeSet?): LinearLayout(context,attrs) {
    private lateinit var trackLengthTextView: TextView
    init{
        inflate(context, R.layout.gps_menu_bar,this)
        val height = convertDpToPixel((resources.getDimension(R.dimen.bottomMapMenu)).toInt())
        layoutParams = ViewGroup.LayoutParams(getScreenWidth(),height)
    }

    fun setEventListener(callbackAdd:(args:Any?)->Unit,
                         callbackDecrease:(args:Any?)->Unit,
                         callbackSave:(args:Any?)->Unit,
                         callbackClear:(args:Any?)->Unit){
        val startGpsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.startGpsBtn)
        val stopGpsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.stopGpsBtn)
        val gpsSavePointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.gpsSavePointsBtn)
        val gpsClearPointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.gpsClearPointsBtn)
        trackLengthTextView = (context as MainActivity).findViewById<TextView>(R.id.gpsCurrentKM)
        startGpsBtn.setCallback(1,callbackAdd)
        stopGpsBtn.setCallback(-1,callbackDecrease)
        gpsSavePointsBtn.setCallback(0,callbackSave)
        gpsClearPointsBtn.setCallback(0,callbackClear)
    }

    @SuppressLint("SetTextI18n")
    fun setTrackLength(trackLength:String){
        trackLengthTextView.text = trackLength
    }

}