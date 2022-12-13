package com.example.runadvisor.activity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.runadvisor.R
import com.example.runadvisor.databinding.ActivityHomeBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.fragment.MapFragment
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.fragmentInstanceToFragment
import com.example.runadvisor.methods.getTitleBarHeight
import com.example.runadvisor.struct.FragmentTracker
import com.example.runadvisor.struct.MapData
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity:AppCompatActivity() {
    private lateinit var bottomNavMenu: BottomNavigationView
    private lateinit var mapData:MapData
    private var fragmentTracker = FragmentTracker()
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setDataBinding()
        setUpNavMenu()
        setEventListener()
    }

    private fun setDataBinding(){
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setUpNavMenu(){
        bottomNavMenu = binding.bottomNavigationView
    }

    private fun setEventListener(){
        bottomNavMenu.setOnItemSelectedListener {it: MenuItem ->
            when(it.itemId){
                R.id.navHome->clearFragments()
                R.id.navMap->navigateFragment(FragmentInstance.FRAGMENT_MAP)
                R.id.navData->navigateFragment(FragmentInstance.FRAGMENT_DATA)
                R.id.navUpload->navigateFragment(FragmentInstance.FRAGMENT_UPLOAD)
            //R.id.navData->moveToActivity(Intent(this, HomeActivity::class.java))
            }
            true
        }
    }

    fun navigateFragment(fragmentInstance: FragmentInstance){
        if(fragmentTracker.currentFragmentIsInstanceOf(fragmentInstance))return
        var frag:Fragment? = fragmentTracker.findOpenFragments(fragmentInstance)
        if(frag == null){
            printToTerminal("frag is null")
            frag = fragmentInstanceToFragment(fragmentInstance)
        }

        fragmentTracker.remove()
        fragmentTracker.push(frag)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.homeLayout,frag).commit()
        }
    }

    private fun clearFragments(){
        if(fragmentTracker.isNotEmpty()){
            supportFragmentManager.beginTransaction().remove(fragmentTracker.root!!).commit()
        }
        fragmentTracker.root = null
    }

    private fun outsideNavMenu(event: MotionEvent):Boolean{ return event.y <bottomNavMenu.y+getTitleBarHeight()}

    /*
    * TODO REMOVE AND CHANGE THIS ONE -> ONLY USED BY MAP...
    * */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        if(fragmentTracker.isEmpty()){return false}
        if(outsideNavMenu(event)){
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    (fragmentTracker.root as IFragment).callbackDispatchTouchEvent(event)
                }
                /*MotionEvent.ACTION_MOVE -> {}
                MotionEvent.ACTION_UP -> {}
                MotionEvent.ACTION_POINTER_DOWN -> {}
                MotionEvent.ACTION_POINTER_UP -> {}
                MotionEvent.ACTION_CANCEL -> {}
                MotionEvent.ACTION_CANCEL -> {}
                */
            }
        }
        return true
    }
}