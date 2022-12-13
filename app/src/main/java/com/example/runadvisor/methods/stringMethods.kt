package com.example.runadvisor.methods
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.fragment.DataFragment
import com.example.runadvisor.fragment.MapFragment
import com.example.runadvisor.fragment.UploadFragment

fun fragmentInstanceToFragment(fragmentInstance:FragmentInstance): Fragment{
    when(fragmentInstance){
        FragmentInstance.FRAGMENT_MAP->{return MapFragment(false,false,FragmentInstance.FRAGMENT_MAP)}
        FragmentInstance.FRAGMENT_MAP_CHILD->{return MapFragment(true,true,FragmentInstance.FRAGMENT_MAP_CHILD)}
        FragmentInstance.FRAGMENT_UPLOAD->{return UploadFragment(false,FragmentInstance.FRAGMENT_UPLOAD)}
        FragmentInstance.FRAGMENT_DATA->{return DataFragment(false,FragmentInstance.FRAGMENT_DATA)}
    }
}

fun getImagePath():String{
    return "images/"
}

fun getUserCollection():String{
    return "UserData"
}

fun getUserItemCollection():String{
    return "RunItems"
}

fun getPublicCollection():String{
    return "PublicData"
}
