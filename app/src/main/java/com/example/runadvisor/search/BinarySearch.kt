package com.example.runadvisor.search
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.SearchInfo

fun insertBetweenClosest(runList:ArrayList<RunItem>,items:List<RunItem>,axis:Int){
    var i = 0
    while(i<items.size){
        val item = items[i]
        val index = findClosestIndex(runList,item,axis)
        if(runList[index].compare(axis)<=item.compare(axis)){runList.add(index+1,item)}
        else{runList.add(index,item)}
        i++
    }
}

fun findClosestIndex(runList: ArrayList<RunItem>,target: RunItem,axis:Int):Int{
    val n = runList.size
    if (target.compare(axis) <= runList[0].compare(axis)){return 0}
    if (target.compare(axis) >= runList[n - 1].compare(axis)){return n - 1}
    var i = 0
    var j = n
    var mid = 0
    while (i < j){
        mid = (i + j) / 2
        if(runList[mid].compare(axis) == target.compare(axis)){return mid}

        if (target.compare(axis) < runList[mid].compare(axis)){
            if(mid > 0 && target.compare(axis) > runList[mid - 1].compare(axis)){
                return getClosest(runList[mid - 1].compare(axis),runList[mid].compare(axis), target.compare(axis),mid-1,mid)
            }
            j = mid
        }
        else{
            if(mid < n - 1 && target.compare(axis) < runList[mid + 1].compare(axis)){
                return getClosest(runList[mid].compare(axis),runList[mid + 1].compare(axis), target.compare(axis),mid,mid+1)
            }
            i = mid + 1
        }
    }

    return mid
}

fun getClosest(v1:Int,v2:Int,target:Int,i1:Int,i2:Int):Int{
    if(target-v1 >= v2-target){return i2}
    return i1
}

fun searchForRunItems(runList:List<RunItem>, key: Int, axis:Int,searchInfo: SearchInfo) {
    var result: Int
    var leftMin: Int
    var rightMax: Int
    val foundIndex: Int
    var foundCount: Int
    var found = false
    leftMin = -1
    rightMax = -1
    foundCount = 0
    foundIndex = searchRunList(runList, key, 0, runList.size - 1,axis)
    if(foundIndex > -1) {
        found = true
        result = foundIndex
        while(searchRunList(runList, key, 0, result - 1,axis).also{result = it} > -1){ leftMin = result}
        result = foundIndex
        while(searchRunList(runList, key, result + 1, runList.size - 1,axis).also{result = it} > -1){ rightMax = result}

        leftMin = if (leftMin == -1) foundIndex else leftMin
        rightMax = if (rightMax == -1) foundIndex else rightMax
        foundCount = rightMax - leftMin + 1
    }
    searchInfo.leftMin = leftMin
    searchInfo.rightMax = rightMax
    searchInfo.foundIndex = foundIndex
    searchInfo.found = found
    searchInfo.foundCount = foundCount
}

fun searchRunList(runList: List<RunItem>, key: Int, first: Int, last: Int,axis:Int): Int {
    var first = first
    var last = last
    var middle: Int
    while(first <= last){
        middle = (first + last) / 2

        if(key < runList[middle].compare(axis)){ last = middle - 1}
        else if(key > runList[middle].compare(axis)){ first = middle + 1}
        else if(key == runList[middle].compare(axis)){ return middle}
    }
    return -1
}