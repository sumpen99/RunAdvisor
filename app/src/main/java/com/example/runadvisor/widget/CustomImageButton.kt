package com.example.runadvisor.widget
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CustomImageButton : androidx.appcompat.widget.AppCompatImageButton {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle)

    @SuppressLint("ClickableViewAccessibility")
    fun setCallback(args:Any?,callback:(args:Any?)->Unit){
        setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {imageAlpha = 127}
                    MotionEvent.ACTION_UP -> {
                        imageAlpha=255
                        callback(args)
                    }
                    MotionEvent.ACTION_CANCEL -> {imageAlpha=255}
                }
                return true
            }
        })
    }

}