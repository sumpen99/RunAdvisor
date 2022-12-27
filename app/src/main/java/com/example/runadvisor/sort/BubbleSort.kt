package com.example.runadvisor.sort
import com.example.runadvisor.struct.RunItem

fun insertBetweenClosest(runList:ArrayList<RunItem>,items:List<RunItem>){
    var i = 0
    while(i<items.size){
        val item = items[i]
        val index = findClosestIndex(runList,item)
        if(runList[index].range<=item.range){runList.add(index+1,item)}
        else{runList.add(Math.max(index-1,0),item)}
        i++
    }
}

fun findClosestIndex(runList: ArrayList<RunItem>,target: RunItem):Int{
    val n = runList.size
    if (target.range <= runList[0].range){return 0}
    if (target.range >= runList[n - 1].range){return n - 1}
    var i = 0
    var j = n
    var mid = 0
    while (i < j){
        mid = (i + j) / 2
        if(runList[mid].range == target.range){return mid}

        if (target.range < runList[mid].range){
            if(mid > 0 && target.range > runList[mid - 1].range){
                return getClosest(runList[mid - 1].range,runList[mid].range, target.range,mid-1,mid)
            }
            j = mid
        }
        else{
            if(mid < n - 1 && target.range < runList[mid + 1].range){
                return getClosest(runList[mid].range,runList[mid + 1].range, target.range,mid,mid+1)
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