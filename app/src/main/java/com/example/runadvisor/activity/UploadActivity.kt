package com.example.runadvisor.activity
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.runadvisor.R
import com.example.runadvisor.enums.FragmentInstance
import com.example.runadvisor.interfaces.IFragment
import com.example.runadvisor.io.printPublicRunItem
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.PublicRunItem
import com.example.runadvisor.struct.UserRunItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.util.*
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import java.io.InputStream

/*
@GlideModule
class AppGlide : AppGlideModule(){

    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry
    ) {
        super.registerComponents(context, glide, registry)
        registry.append(
            StorageReference::class.java, InputStream::class.java,
            FirebaseImageLoader.Factory()
        )

    }
}

class UploadActivity : AppCompatActivity() {
    private lateinit var bottomNavMenu: BottomNavigationView
    private var currentFragment: Fragment? = null
    private var _binding: ActivityUploadBinding? = null
    private val binding get() = _binding!!
    private val GALLERY_REQUEST_CODE = 102
    private val PICK_IMAGE = 1
    private var fileUri:Uri? = null
    private var filePath:String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)
        setDataBinding()
        setUpNavMenu()
        setEventListener(findViewById(R.id.uploadLayout))
        //user = Firebase.auth.currentUser!!
        //loadData()
    }

    private fun loadData(){
        val docRef = Firebase.firestore.collection("PublicData")
        docRef.get().addOnSuccessListener { documentSnapShot->
            for(document in documentSnapShot.documents){
                val runItem = document.toObject<PublicRunItem>()
                printPublicRunItem(runItem!!)
            }
        }
    }

    private fun clearLastUpload(){
        filePath = null
        fileUri = null
    }

    private fun setDataBinding(){
        _binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setUpNavMenu(){
        bottomNavMenu = binding.bottomNavigationView
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view: View){
        binding.uploadItemBtn.setOnClickListener{ uploadUserRunItem()}
        binding.loadImageBtn.setOnClickListener{selectImageFromGallery(PICK_IMAGE)}
        view.setOnTouchListener { v, event ->
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {binding.cityText.hideKeyboard();binding.streetText.hideKeyboard()}
                //MotionEvent.ACTION_POINTER_DOWN -> {}
                //MotionEvent.ACTION_MOVE -> {}
                //MotionEvent.ACTION_UP -> {}
                //MotionEvent.ACTION_POINTER_UP -> {}
                //MotionEvent.ACTION_CANCEL -> {}
            }
            true
        }

        bottomNavMenu.setOnItemSelectedListener {it: MenuItem ->
            when(it.itemId){
                R.id.navHome->removeCurrentFragment()
                R.id.navMap->navigateFragment(FragmentInstance.FRAGMENT_MAP)
                //R.id.navData->moveToActivity(Intent(this, HomeActivity::class.java))
            }
            true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE
            && resultCode == Activity.RESULT_OK
            && data != null
            && data.data != null
        ) {
            filePath = getFilePathFromIntent(data)
            fileUri = data.data!!
            loadImageFromPhone(fileUri.toString())
        }
    }

    private fun uploadUserRunItem(){
        if(validUploadData()){
            val user = Firebase.auth.currentUser
            val city = binding.cityText.text.toString()
            val street = binding.streetText.text.toString()
            val urlId = UUID.randomUUID().toString()
            val downloadUrl = urlId + ".${filePath!!.split(".")[1]}"
            val item = UserRunItem(city,street,true,UUID.randomUUID().toString(),downloadUrl)
            Firebase.firestore
                .collection("UserData")
                .document(user!!.uid)
                .collection("RunItems").
                add(item).addOnCompleteListener { task ->
                    if(task.isSuccessful && item.shareWithPublic){uploadPublicRunItem(item)}
                    else{Toast.makeText(baseContext, "Upload Failed ${task.exception}",Toast.LENGTH_SHORT).show()}
                }
        }
    }

    private fun uploadPublicRunItem(userItem:UserRunItem){
        val publicItem = PublicRunItem(userItem.city,userItem.street,userItem.downloadUrl)
        Firebase.firestore
            .collection("PublicData")
            .document(userItem.runItemID)
            .set(publicItem).addOnCompleteListener { task ->
                if(task.isSuccessful) {uploadImageToFirebase(publicItem)}
                else{Toast.makeText(baseContext, task.exception.toString(),Toast.LENGTH_SHORT).show()}
            }

    }

    private fun uploadImageToFirebase(item:PublicRunItem) {
        val database = Firebase.storage.reference
        val storageRef = database.child("images/${item.downloadUrl}")
        storageRef.putFile(fileUri!!)
            .addOnCompleteListener { task ->
                var msg = "Item Uploaded"
                if(!task.isSuccessful){msg = task.exception.toString()}
                Toast.makeText(baseContext,msg,Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadImageFromFirebase(item:PublicRunItem){
        val database = Firebase.storage.reference
        val storageRef = database.child("images/${item.downloadUrl}")
        loadImageFromStorage(storageRef)
    }

    private fun loadImageFromStorage(storeRef:StorageReference){
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        GlideApp.with(this)
            .load(storeRef)
            .error(R.drawable.ic_load_error_foreground)
            .circleCrop()
            .override(200, 200)
            .transition(withCrossFade(factory))
            .into(binding.imageView)
    }

    private fun loadImageFromPhone(imagePath:String){
        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        GlideApp.with(this)
            .load(imagePath)
            .error(R.drawable.ic_load_error_foreground)
            .circleCrop()
            .override(200, 200)
            .transition(withCrossFade(factory))
            .into(binding.imageView)
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

            replace(R.id.uploadLayout,currentFragment!!).commit()
        }
    }

    private fun removeCurrentFragment(){
        if(currentFragment!=null){
            supportFragmentManager.beginTransaction().remove(currentFragment!!).commit()
            currentFragment = null
        }
    }

    private fun outsideNavMenu(event:MotionEvent):Boolean{ return event.y <bottomNavMenu.y+getTitleBarHeight()}

    private fun validUploadData():Boolean{
        return (Firebase.auth.currentUser!=null &&
            binding.cityText.text.toString().isNotEmpty() &&
            binding.streetText.text.toString().isNotEmpty() &&
                filePath != null &&
                fileUri != null)
    }

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

 */