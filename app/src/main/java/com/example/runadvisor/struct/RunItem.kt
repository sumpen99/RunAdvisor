package com.example.runadvisor.struct

data class RunItem(
    val city:String?=null,
    val street:String?=null,
    val trackLength:String?=null,
    val downloadUrl:String?=null,
    val coordinates:ArrayList<Double>? = null,
    val center:ArrayList<Double>? = null,
    val zoom:Int? = null,
    val docID:String? = null,
    var range:Double = 0.0,
    ):java.io.Serializable
