package com.example.runadvisor.methods
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.fragment.DataFragment
import com.example.runadvisor.fragment.MapFragment
import com.example.runadvisor.fragment.UploadFragment

fun fragmentInstanceToFragment(fragmentInstance:FragmentInstance): Fragment?{
    when(fragmentInstance){
        FragmentInstance.FRAGMENT_MAP->{return MapFragment()}
        FragmentInstance.FRAGMENT_UPLOAD->{return UploadFragment()}
        FragmentInstance.FRAGMENT_DATA->{return DataFragment()}
    }
}

fun getImagePath():String{
    return "images/"
}
