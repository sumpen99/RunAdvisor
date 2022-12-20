package com.example.runadvisor.struct

data class RunItem(
    val city:String?=null,
    val street:String?=null,
    val trackLength:String?=null,
    val downloadUrl:String?=null,
    val coordinates:ArrayList<Double>,
    val center:ArrayList<Double>
    )
