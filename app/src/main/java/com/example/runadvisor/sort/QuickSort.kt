package com.example.runadvisor.sort

import com.example.runadvisor.struct.RunItem

fun qSortRunItems(runItems:List<RunItem>,low:Int,high:Int){
    sortRunItemList(runItems,low,high)
}

private fun sortRunItemList(runItemList:List<RunItem>,low:Int,high:Int){
    if(low<high){
        val q = partitionRunItemList(runItemList,low,high)
        sortRunItemList(runItemList,low,q)
        sortRunItemList(runItemList,q+1,high)
    }
}

private fun partitionRunItemList(runItemList:List<RunItem>,low:Int,high:Int):Int{
    val pivot = runItemList[low].range
    var i = low-1
    var j = high+1
    while(true){
        while(++i < high && runItemList[i].range < pivot);
        while(--j > low && runItemList[j].range > pivot);
        if(i < j){swapRunItem(runItemList[i],runItemList[j]);}
        else{return j;}
    }
}

private fun swapRunItem(r1: RunItem,r2: RunItem) {
    val city = r1.city
    val street = r1.street
    val trackLength = r1.trackLength
    val downloadUrl = r1.downloadUrl
    val coordinates = r1.coordinates
    val center = r1.center
    val zoom = r1.zoom
    val docID = r1.docID
    var range = r1.range
    r1.swapValues(r2.city,r2.street,r2.trackLength,r2.downloadUrl,r2.coordinates,r2.center,r2.zoom,r2.docID,r2.range)
    r2.swapValues(city,street,trackLength,downloadUrl,coordinates,center,zoom,docID,range)
}