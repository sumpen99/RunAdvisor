package com.example.runadvisor.interfaces

import android.view.MotionEvent
import com.example.runadvisor.enums.FragmentInstance

interface IFragment {
    fun callbackDispatchTouchEvent(event: MotionEvent)
    fun processWork(parameter:Any?)
    fun getFragmentID(): FragmentInstance
}