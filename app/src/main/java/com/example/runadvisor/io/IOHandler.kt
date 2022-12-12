package com.example.runadvisor.io
import android.content.Intent
import android.util.Log
import android.view.MotionEvent
import com.example.runadvisor.struct.PublicRunItem

fun printMotionEvent(event:MotionEvent){
    printToTerminal("X: ${event.x} Y: ${event.y}")
}

fun printXYCoordinate(x:Float,y:Float){
    printToTerminal("X: $x Y: $y")
}

fun printActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
    if(data==null){
        printToTerminal("RequestCode: $requestCode ResultCode: $resultCode Data: Null")
    }
    else{
        printToTerminal("RequestCode: $requestCode ResultCode: $resultCode Data: ${data.toString()}")
    }

}

fun printPublicRunItem(item:PublicRunItem){
    printToTerminal("${item.city} ${item.street}")
}

fun printToTerminal(msg:String){
    Log.d("Message",msg)
}