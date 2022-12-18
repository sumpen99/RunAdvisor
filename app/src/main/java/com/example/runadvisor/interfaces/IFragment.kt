package com.example.runadvisor.interfaces
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance

interface IFragment {
    fun callbackDispatchTouchEvent(event: MotionEvent)
    fun receivedData(parameter:Any?)
    fun getFragmentID(): FragmentInstance
    fun isRemovable():Boolean
}