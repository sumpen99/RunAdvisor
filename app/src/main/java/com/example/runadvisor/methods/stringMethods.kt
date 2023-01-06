package com.example.runadvisor.methods
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.fragment.*
import java.text.SimpleDateFormat
import java.util.*

fun fragmentInstanceToFragment(fragmentInstance:FragmentInstance): Fragment{
    when(fragmentInstance){
        FragmentInstance.FRAGMENT_MAP_TRACK_OVERVIEW->{return MapFragmentViewAll()}
        FragmentInstance.FRAGMENT_MAP_TRACK_ITEM->{return MapFragmentViewItem()}
        FragmentInstance.FRAGMENT_MAP_TRACK_PATH->{return MapFragmentUpload()}
        FragmentInstance.FRAGMENT_UPLOAD->{return UploadFragment()}
        FragmentInstance.FRAGMENT_DATA->{return DataFragment()}
        FragmentInstance.FRAGMENT_USER->{return UserFragment()}
    }
}

fun Double.inKilometers():String{
    return (this/1000).format(4)
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun getCurrentDate():String{
    val myDate = Date()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
    return dateFormat.format(myDate)
}
