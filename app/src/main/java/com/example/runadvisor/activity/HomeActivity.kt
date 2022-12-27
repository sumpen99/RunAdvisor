package com.example.runadvisor.activity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.runadvisor.R
import com.example.runadvisor.database.FirestoreViewModel
import com.example.runadvisor.databinding.ActivityHomeBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.FragmentTracker
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.widget.CustomDataAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView


class HomeActivity:AppCompatActivity() {
    private lateinit var bottomNavMenu: BottomNavigationView
    lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var customAdapter:CustomDataAdapter
    private var fragmentTracker = FragmentTracker()
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private val LOCATION_PERMISSION_CODE = 2

    private var observer = Observer<List<RunItem>?>{ it->
        if(it!=null){
            var i = 0
            while(i<it.size){
                val trackGeo = getDoubleToGeoPoint(it[i].center!!)
                val userGeo = getUserLocation()
                it[i++].range = latLonToMeter(trackGeo,userGeo).toInt()
            }
            customAdapter.addRunItems(it)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setViewModel()
        setDataBinding()
        setUpNavMenu()
        setEventListener()
        setAdapter()
        askForStoragePermissions()
        askForLocationPermission()
        navigateFragment(FragmentInstance.FRAGMENT_DATA)
    }

    private fun setDataBinding(){
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setAdapter(){
        customAdapter = CustomDataAdapter(this)
    }

    private fun setUpNavMenu(){
        bottomNavMenu = binding.bottomNavigationView
    }

    private fun setEventListener(){
        bottomNavMenu.setOnItemSelectedListener {it: MenuItem ->
            when(it.itemId){
                R.id.navHome->navigateFragment(FragmentInstance.FRAGMENT_DATA)
                R.id.navMap->navigateFragment(FragmentInstance.FRAGMENT_MAP_TRACK_OVERVIEW)
                R.id.navUpload->navigateFragment(FragmentInstance.FRAGMENT_UPLOAD)
                R.id.navUser->navigateFragment(FragmentInstance.FRAGMENT_USER)
                //R.id.navData->moveToActivity(Intent(this, HomeActivity::class.java))
            }
            true
        }
    }

    private fun setViewModel(){
        firestoreViewModel = ViewModelProviders.of(this).get(FirestoreViewModel::class.java)
    }

    fun getAdapter():CustomDataAdapter{
        return customAdapter
    }

    /*
    *   ##########################################################################
    *               ASK FOR PERMISSIONS
    *   ##########################################################################
    * */

    private fun askForLocationPermission(){
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                this,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_CODE
            )
        }

    }

    private fun askForStoragePermissions(){
        if(ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        )
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /*
    *   ##########################################################################
    *               NAVIGATE BETWEEN FRAGMENTS
    *   ##########################################################################
    * */

    fun pushDataToFragment(fragmentInstance: FragmentInstance,parameter:Any){
        val frag:Fragment? = fragmentTracker.findOpenFragments(fragmentInstance)
        if(frag!=null){(frag as IFragment).receivedData(parameter)}
    }

    fun navigateFragment(fragmentInstance: FragmentInstance,params:Any?=null){
        if(fragmentTracker.currentFragmentIsInstanceOf(fragmentInstance))return
        var frag:Fragment? = fragmentTracker.findOpenFragments(fragmentInstance)
        if(frag == null){
            frag = fragmentInstanceToFragment(fragmentInstance)
            if(params!=null){(frag as IFragment).receivedData(params)}
        }

        fragmentTracker.remove()
        fragmentTracker.push(frag)

        applyTransaction(frag)
    }

    private fun applyTransaction(frag:Fragment){
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
    *   ##########################################################################
    *               TALK TO FIREBASE
    *   ##########################################################################
    * */

    private fun setObservableData(){
        firestoreViewModel.getRunItems().observe(this,observer)
    }

    private fun cancelObservableData(){
        firestoreViewModel.getRunItems().removeObserver(observer)
    }

    fun runItemsIsNotNull():Boolean{
        return customAdapter.itemCount>0
    }

    fun getRunItems():List<RunItem>{
        return customAdapter.serverData
    }


    /*
    *   ##########################################################################
    *               REMOVE DISPATCH FUNCTION
    *   ##########################################################################
    * */

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        if(fragmentTracker.isEmpty()){return false}
        if(outsideNavMenu(event)){
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    (fragmentTracker.root as IFragment).callbackDispatchTouchEvent(event)
                }
                /*MotionEvent.ACTION_MOVE -> {
                    (fragmentTracker.root as IFragment).callbackDispatchTouchEvent(event)
                }*/
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

    override fun onResume() {
        super.onResume()
        setObservableData()
    }

    override fun onPause() {
        super.onPause()
        cancelObservableData()
    }

}