package com.example.runadvisor.struct

import com.example.runadvisor.enums.ServerResult

class ServerDetails(var pos:Int, var msg:String,var serverResult:ServerResult){
    constructor():this(0,"",ServerResult.UPLOAD_OK)
}