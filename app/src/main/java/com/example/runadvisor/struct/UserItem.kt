package com.example.runadvisor.struct

class UserItem(val docId:String?=null,val downloadUrl:String?=null){
    override fun toString(): String {
        return "DocId: $docId DownloadUrl: $downloadUrl"
    }
}