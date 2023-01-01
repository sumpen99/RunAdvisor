package com.example.runadvisor.widget
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.runadvisor.R
import com.example.runadvisor.MainActivity
import com.example.runadvisor.methods.*

class TrackMenuBar(
    context: Context?,
    attrs: AttributeSet?):LinearLayout(context,attrs) {
    private lateinit var trackLengthTextView:TextView
    init{
        inflate(context, R.layout.track_menu_bar,this)
        val height = convertDpToPixel((resources.getDimension(R.dimen.bottomMapMenu)).toInt())
        layoutParams = ViewGroup.LayoutParams(getScreenWidth(),height)
    }

    fun setEventListener(callbackAdd:(args:Any?)->Unit,
                         callbackDecrease:(args:Any?)->Unit,
                         callbackSave:(args:Any?)->Unit,
                         callbackClear:(args:Any?)->Unit){
        val addPointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.addLassoPoints)
        val decreasePointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.decreaseLassoPoints)
        val saveLassoPointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.saveLassoPoints)
        val clearLassoPointsBtn = (context as MainActivity).findViewById<CustomImageButton>(R.id.clearLassoPoints)
        trackLengthTextView = (context as MainActivity).findViewById<TextView>(R.id.lassoCurrentKM)
        addPointsBtn.setCallback(1,callbackAdd)
        decreasePointsBtn.setCallback(-1,callbackDecrease)
        saveLassoPointsBtn.setCallback(0,callbackSave)
        clearLassoPointsBtn.setCallback(0,callbackClear)
    }

    @SuppressLint("SetTextI18n")
    fun setTrackLength(trackLength:String){
        trackLengthTextView.text = "$trackLength km"
    }

}