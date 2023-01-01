package com.example.runadvisor.struct
import androidx.fragment.app.Fragment
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal

class FragmentTracker {
    var root: Fragment?=null
    var fragmentInUse = mutableMapOf <String,Fragment>()

    fun findOpenFragments(fragmentInstance: FragmentInstance):Fragment?{
        return fragmentInUse[fragmentInstance.name]
    }

    fun push(fragment:Fragment){
        val fragInstance = (fragment as IFragment).getFragmentID()
        if(!fragmentInUse.containsKey(fragInstance.name)){
            fragmentInUse[fragInstance.name] = fragment
        }
        root = fragment
    }

    fun remove(){
        if(root!=null){
            if((root as IFragment).isRemovable()){
                fragmentInUse.remove((root as IFragment).getFragmentID().name)
            }
        }
    }

    fun clear(){
        root = null
    }

    fun currentFragmentNeedDispatch():Boolean{
        if(root!=null){
            return (root as IFragment).needDispatch()
        }
        return false
    }

    fun currentFragmentHasParent():FragmentInstance?{
        if(root!=null){
            return (root as IFragment).hasParentFragment()
        }
        return null
    }

    fun currentFragmentIsInstanceOf(fragmentInstance: FragmentInstance):Boolean{
        if(root!=null){
            return fragmentInstance == (root as IFragment).getFragmentID()
        }
        return false
    }

    fun isNotEmpty():Boolean{
        return root != null
    }

    fun isEmpty():Boolean{
        return root == null
    }
}