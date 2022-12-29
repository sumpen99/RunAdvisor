package com.example.runadvisor.struct

class SearchInfo{
    var found:Boolean = false
    var leftMin:Int = -1
    var rightMax:Int = -1
    var foundIndex:Int = -1
    var foundCount:Int = -1

    override fun toString(): String {
        return "found: $found leftMin: $leftMin rightMax: $rightMax foundIndex: $foundIndex foundCount: $foundCount"
    }
}