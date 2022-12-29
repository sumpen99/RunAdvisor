package com.example.runadvisor
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runadvisor.activity.LoginActivity
import com.example.runadvisor.adapter.CustomDownloadAdapter
import com.example.runadvisor.adapter.CustomUserAdapter
import com.example.runadvisor.database.FirestoreViewModel
import com.example.runadvisor.databinding.ActivityMainBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.FragmentTracker
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.UserItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavMenu: BottomNavigationView
    lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var customPublicAdapter: CustomDownloadAdapter
    private lateinit var customUserAdapter: CustomUserAdapter
    private lateinit var userGeo:GeoPoint
    private var fragmentTracker = FragmentTracker()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var AXIS = 0

    private var publicObserver = Observer<List<RunItem>?>{ it->
        if(it!=null){
            var i = 0
            while(i<it.size){
                //val trackGeo = getDoubleToGeoPoint(it[i].center!!)
                //val userGeo = getUserLocation()
                it[i].range = latLonToMeter(getDoubleToGeoPoint(it[i].center!!),userGeo).toInt()
                i++
            }
            customPublicAdapter.addRunItems(it)
        }
    }

    private var userObserver = Observer<List<UserItem>?>{ it->
        if(it!=null){
            var i = 0
            while(i<it.size){
                val userItem = it[i++]
                firestoreViewModel.
                firebaseRepository.
                getSavedPublicRunItem(userItem.docId).
                get().
                addOnCompleteListener{task->
                    if(task.isSuccessful){
                        for(doc: DocumentSnapshot in task.result.documents){
                            val runItem = doc.toObject(RunItem::class.java)
                            customUserAdapter.addUserItem(runItem!!)
                        }
                    }
                }
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        printToTerminal("creating home")
        verifyPermissions()
        if(isUserActive()){
            setContentView(R.layout.activity_main)
            setUserGeoLocation()
            setViewModel()
            setDataBinding()
            setUpNavMenu()
            setEventListener()
            setPublicAdapter()
            setUserAdapter()
            setObservable()
        }
    }

    private fun setDataBinding(){
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setPublicAdapter(){
        customPublicAdapter = CustomDownloadAdapter(this)
    }

    private fun setUserAdapter(){
        customUserAdapter = CustomUserAdapter(this)
    }

    private fun setUpNavMenu(){
        bottomNavMenu = binding.bottomNavigationView
    }

    private fun isUserActive():Boolean{
        return Firebase.auth.currentUser!=null
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

    private fun setUserGeoLocation(){
        userGeo = getUserLocation()
    }

    fun getPublicAdapter(): CustomDownloadAdapter {
        return customPublicAdapter
    }

    fun getUserAdapter(): CustomUserAdapter {
        return customUserAdapter
    }

    fun getSearchAxis():Int{
        return AXIS
    }

    fun setSearchAxis(axis:Int){
        AXIS = axis
    }

    /*
    *   ##########################################################################
    *               ASK FOR PERMISSIONS
    *   ##########################################################################
    * */

    private fun verifyPermissions(){
        if(!verifyStoragePermission()){askForStoragePermissions()}
        if(!verifyLocationPermission()){askForLocationPermission()}
    }

    /*
    *   ##########################################################################
    *               NAVIGATE BETWEEN ACTIVITY AND FRAGMENTS
    *   ##########################################################################
    * */

    fun pushDataToFragment(fragmentInstance: FragmentInstance, parameter:Any){
        val frag: Fragment? = fragmentTracker.findOpenFragments(fragmentInstance)
        if(frag!=null){(frag as IFragment).receivedData(parameter)}
    }

    fun navigateFragment(fragmentInstance: FragmentInstance, params:Any?=null){
        if(fragmentTracker.currentFragmentIsInstanceOf(fragmentInstance))return
        var frag: Fragment? = fragmentTracker.findOpenFragments(fragmentInstance)
        if(frag == null){
            frag = fragmentInstanceToFragment(fragmentInstance)
            if(params!=null){(frag as IFragment).receivedData(params)}
        }

        fragmentTracker.remove()
        fragmentTracker.push(frag)

        applyTransaction(frag)
    }

    private fun navigateOnResume(){
        if(Firebase.auth.currentUser!=null){
            if(fragmentTracker.isNotEmpty()){applyTransaction(fragmentTracker.root!!)}
            else{navigateFragment(FragmentInstance.FRAGMENT_DATA)}
        }
        else{
            moveToActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun applyTransaction(frag: Fragment){
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

    private fun checkObservers(){
        printToTerminal(firestoreViewModel.getUserRunItems().hasObservers().toString())
        printToTerminal(firestoreViewModel.getPublicRunItems(::removePublicRunItemFromAdapter).hasObservers().toString())
    }

    private fun setObservablePublicData(){
        firestoreViewModel.getPublicRunItems(::removePublicRunItemFromAdapter).observe(this,publicObserver)
    }

    private fun cancelObservablePublicData(){
        firestoreViewModel.getPublicRunItems(::removePublicRunItemFromAdapter).removeObserver(publicObserver)
    }

    private fun setObservableUserData(){
        firestoreViewModel.getUserRunItems().observe(this,userObserver)
    }

    private fun cancelObservableUserData(){
        firestoreViewModel.getUserRunItems().removeObserver(userObserver)
    }

    private fun setObservable(){
        setObservablePublicData()
        setObservableUserData()
    }

    private fun cancelObservable(){
        cancelObservablePublicData()
        cancelObservableUserData()
    }

    fun runItemsIsNotNull():Boolean{
        return customPublicAdapter.itemCount>0
    }

    fun getRunItems():List<RunItem>{
        return customPublicAdapter.serverData
    }

    fun getImageStorageRef(downloadUrl:String?): StorageReference {
        return firestoreViewModel.firebaseRepository.getImageStorageReference(downloadUrl)
    }

    private fun removePublicRunItemFromAdapter(runItem: RunItem){
        runItem.range = latLonToMeter(getDoubleToGeoPoint(runItem.center!!),userGeo).toInt()
        customPublicAdapter.removeRunItem(runItem)
    }

    fun removeRunItemFromFirebase(runItem: RunItem){
        firestoreViewModel.deleteImage(runItem.downloadUrl!!)
        firestoreViewModel.deletePublicRunItem(runItem.docID!!)
        firestoreViewModel.deleteUserRunItem(runItem.docID!!)
    }

    fun userItemsIsNotNull():Boolean{
        return customUserAdapter.itemCount > 0
    }

    fun getUserItems():List<RunItem>{
        return customUserAdapter.userData
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
                MotionEvent.ACTION_SCROLL->{
                    printToTerminal("scrolling")
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

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        printToTerminal("onRestoreInstanceState")
    }

    override fun onResume() {
        super.onResume()
        //isUserActive()
        //setObservable()
        //checkObservers()
        navigateOnResume()
        printToTerminal("resume")
    }

    override fun onPause() {
        super.onPause()
        //cancelObservable()
        printToTerminal("paus")
    }

    override fun onStop() {
        super.onStop()
        //cancelObservable()
        printToTerminal("stop")
    }

    override fun onDestroy() {
        super.onDestroy()
        printToTerminal("destroy")
    }

}