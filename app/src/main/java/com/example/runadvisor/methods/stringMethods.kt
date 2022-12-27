package com.example.runadvisor.methods
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.fragment.*
import java.text.SimpleDateFormat
import java.util.*

fun fragmentInstanceToFragment(fragmentInstance:FragmentInstance): Fragment{
    when(fragmentInstance){
        FragmentInstance.FRAGMENT_MAP_TRACK_OVERVIEW->{return MapFragmentTrackOverview()}
        FragmentInstance.FRAGMENT_MAP_TRACK_ITEM->{return MapFragmentTrackItem()}
        FragmentInstance.FRAGMENT_MAP_TRACK_PATH->{return MapFragmentTrackPath()}
        FragmentInstance.FRAGMENT_UPLOAD->{return UploadFragment()}
        FragmentInstance.FRAGMENT_DATA->{return DataFragment()}
        FragmentInstance.FRAGMENT_USER->{return UserFragment()}
    }
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun getImagePath():String{
    return "images/"
}

fun getUserCollection():String{
    return "Users"
}

fun getItemCollection():String{
    return "RunItems"
}

fun getUserRunItemsCollection():String{
    return "UserRunItems"
}

fun getPublicRunItemsCollection():String{
    return "PublicRunItems"
}

fun getCurrentDate():String{
    val myDate = Date()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
    return dateFormat.format(myDate)
}
