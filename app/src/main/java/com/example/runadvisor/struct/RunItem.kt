package com.example.runadvisor.struct

data class RunItem(
    var city:String?=null,
    var street:String?=null,
    var trackLength:String?=null,
    var downloadUrl:String?=null,
    var coordinates:ArrayList<Double>? = null,
    var center:ArrayList<Double>? = null,
    var zoom:Int? = null,
    var docID:String? = null,
    var range:Double = 0.0,
    ):java.io.Serializable{
        fun swapValues(city:String?,
                       street:String?,
                       trackLength:String?,
                       downloadUrl:String?,
                       coordinates:ArrayList<Double>?,
                       center:ArrayList<Double>?,
                       zoom:Int?,
                       docId:String?,
                       range:Double){
            this.city = city
            this.street = street
            this.trackLength = trackLength
            this.downloadUrl = downloadUrl
            this.coordinates = coordinates
            this.center = center
            this.zoom = zoom
            this.docID = docId
            this.range = range
        }
    }
