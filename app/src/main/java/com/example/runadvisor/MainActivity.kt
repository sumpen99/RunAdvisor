package com.example.runadvisor
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.runadvisor.activity.LoginActivity
import com.example.runadvisor.adapter.CustomDownloadAdapter
import com.example.runadvisor.adapter.CustomUserAdapter
import com.example.runadvisor.database.FirestoreViewModel
import com.example.runadvisor.databinding.ActivityMainBinding
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.enums.SortOperation
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.FragmentTracker
import com.example.runadvisor.struct.MessageToUser
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
    private lateinit var onBackPressedCallback:OnBackPressedCallback
    lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var customPublicAdapter: CustomDownloadAdapter
    private lateinit var customUserAdapter: CustomUserAdapter
    private lateinit var userGeo:GeoPoint
    private lateinit var messageToUser: MessageToUser
    private var fragmentTracker = FragmentTracker()
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private var sortOperation = SortOperation.SORT_RANGE
    private var PERMISSION_RESULT = 0

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
        if(isUserActive()){
            setContentView(R.layout.activity_main)
            setDataBinding()
            verifyPermissions()
            //setUserGeoLocation()
            //setViewModel()
            //setUpNavMenu()
            //setOnBackNavigation()
            //setEventListener()
            //setPublicAdapter()
            //setUserAdapter()
            //setObservable()

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

    private fun setOnBackNavigation(){
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){navigateOnBackPressed()}
        }
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
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

    private fun setInfoToUser(){
        messageToUser = MessageToUser(this,null)
    }

    fun getPublicAdapter(): CustomDownloadAdapter {
        return customPublicAdapter
    }

    fun getUserAdapter(): CustomUserAdapter {
        return customUserAdapter
    }

    fun getSearchAxis():SortOperation{
        return sortOperation
    }

    fun getUserIconTag():String{
        return Firebase.auth.currentUser!!.uid + getString(R.string.user_icon)
    }

    fun getUserNameTag():String{
        return Firebase.auth.currentUser!!.uid + getString(R.string.user_name)
    }

    fun setSearchAxis(op:SortOperation){
        sortOperation = op
        customPublicAdapter.sortRunItems()
    }

    /*
    *   ##########################################################################
    *               SHOW USER MESSAGE
    *   ##########################################################################
    * */

    fun showUserMessage(msg:String){
        messageToUser.setMessage(msg)
        messageToUser.showMessage()
    }

    /*
    *   ##########################################################################
    *               ASK FOR PERMISSIONS
    *   ##########################################################################
    * */

    private fun verifyPermissions(){
        if(!verifyLocationPermission()){askForLocationPermission()}
        else{PERMISSION_RESULT+=1}
        if(!verifyStoragePermission()){askForStoragePermissions()}
        else{PERMISSION_RESULT+=1}
        onAllPermissionCheck()
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var i = 0
        when(requestCode) {
            LOCATION_PERMISSION_CODE -> {
                /*if(grantResults.isNotEmpty()){
                    for(result:Int in grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            // not good
                        }
                    }
                }
                else{
                    //probably not good
                }*/
            }
            DATA_PERMISSIONS_CODE -> {
                /*if(grantResults.isNotEmpty()){
                    for(result:Int in grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            // not good
                        }
                    }
                }
                else{
                    //probably not good
                }*/
            }
        }
        PERMISSION_RESULT+=1
        onAllPermissionCheck()
    }

    private fun onAllPermissionCheck(){
        if(PERMISSION_RESULT == ALL_PERMISSIONS_CHECKED ){
            setUpNavMenu()
            setInfoToUser()
            setOnBackNavigation()
            setEventListener()
            setUserGeoLocation()
            setViewModel()
            setPublicAdapter()
            setUserAdapter()
            setObservable()
        }
     }

    /*
    *   ##########################################################################
    *               NAVIGATE BETWEEN FRAGMENTS
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

    private fun navigateOnBackPressed(){
        val frag = fragmentTracker.currentFragmentHasParent()
        if(frag!=null){navigateFragment(frag,null)}
    }

    private fun navigateOnResume(){
        if(Firebase.auth.currentUser==null){moveToActivity(Intent(this,LoginActivity::class.java));return}
        else if(PERMISSION_RESULT < ALL_PERMISSIONS_CHECKED){return}

        if(fragmentTracker.isNotEmpty()){applyTransaction(fragmentTracker.root!!)}
        else{navigateFragment(FragmentInstance.FRAGMENT_DATA)}
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

    fun cancelObservable(){
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
    *                DISPATCH FUNCTION NEEDED FOR MAPFRAGMENT (MAPVIEW)
    *   ##########################################################################
    * */

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)
        if(dispatchIsNeeded(event)){
            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    //printToTerminal("ACTION_DOWN")
                }
                MotionEvent.ACTION_MOVE -> {
                    (fragmentTracker.root as IFragment).callbackDispatchTouchEvent(null)
                    //printToTerminal("ACTION_MOVE")
                    //(fragmentTracker.root as IFragment).callbackDispatchTouchEvent(null)
                }
                MotionEvent.ACTION_UP -> {
                    //printToTerminal("ACTION_UP")
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    //printToTerminal("ACTION_POINTER_DOWN")
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    //printToTerminal("ACTION_POINTER_UP")
                }
                MotionEvent.ACTION_CANCEL -> {
                    //printToTerminal("cancel")
                }

            }
        }
        return true
    }

    private fun dispatchIsNeeded(event:MotionEvent):Boolean{
        return  fragmentTracker.currentFragmentNeedDispatch() && outsideNavMenu(event)
    }

    override fun onResume() {
        super.onResume()
        navigateOnResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        printToTerminal("on stop main")
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}