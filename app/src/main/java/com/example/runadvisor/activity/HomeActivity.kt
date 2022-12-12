package com.example.runadvisor.activity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.runadvisor.R
import com.example.runadvisor.databinding.ActivityHomeBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.fragmentInstanceToFragment
import com.example.runadvisor.methods.getTitleBarHeight
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity:AppCompatActivity() {
    private lateinit var bottomNavMenu: BottomNavigationView
    private var currentFragment: Fragment? = null
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
                R.id.navHome->removeCurrentFragment()
                R.id.navMap->navigateFragment(FragmentInstance.FRAGMENT_MAP)
                R.id.navData->navigateFragment(FragmentInstance.FRAGMENT_DATA)
                R.id.navUpload->navigateFragment(FragmentInstance.FRAGMENT_UPLOAD)
            //R.id.navData->moveToActivity(Intent(this, HomeActivity::class.java))
            }
            true
        }
    }

    private fun currentFragmentIsInstanceOf(fragmentInstance:FragmentInstance):Boolean{
        if(currentFragment!=null){
            return fragmentInstance == (currentFragment as IFragment).getFragmentID()
        }
        return false
    }

    private fun navigateFragment(fragmentInstance: FragmentInstance){
        if(currentFragmentIsInstanceOf(fragmentInstance)){return}
        supportFragmentManager.beginTransaction().apply {
            currentFragment = fragmentInstanceToFragment(fragmentInstance)
            replace(R.id.homeLayout,currentFragment!!).commit()
        }
    }

    private fun removeCurrentFragment(){
        if(currentFragment!=null){
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commit()
            currentFragment = null
        }
    }

    private fun outsideNavMenu(event: MotionEvent):Boolean{ return event.y <bottomNavMenu.y+getTitleBarHeight()}

    /*
    * TODO REMOVE AND CHANGE THIS ONE -> ONLY USED BY MAP...
    * */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        if(currentFragment == null){return false}
        if(outsideNavMenu(event)){
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    (currentFragment as IFragment).callbackDispatchTouchEvent(event)
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