package com.example.runadvisor.interfaces
import com.example.runadvisor.enums.FragmentInstance

interface IFragment {
    fun callbackDispatchTouchEvent(parameter:Any?)
    fun receivedData(parameter:Any?)
    fun getFragmentID(): FragmentInstance
    fun isRemovable():Boolean
    fun needDispatch():Boolean
}