package com.example.runadvisor.methods

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.runadvisor.R
import com.example.runadvisor.struct.MessageToUser
import com.example.runadvisor.struct.RunItem
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

/*
*   ##########################################################################
*                            APP DIMENSIONS
*   ##########################################################################
*
* */

fun getScreenWidth() : Int{
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight() : Int{
    return Resources.getSystem().displayMetrics.heightPixels
}

@SuppressLint("InternalInsetResource")
fun Activity.getTitleBarHeight():Int{
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if(resourceId>0){
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun Activity.removeActionBarHeight():Float{
    val styledAttributes = baseContext.theme.obtainStyledAttributes(
        intArrayOf(android.R.attr.actionBarSize)
    )
    val mActionBarSize = styledAttributes.getDimension(0,0.0f)
    styledAttributes.recycle()
    return mActionBarSize
}

fun convertDpToPixel(value : Int):Int{
    return (value* Resources.getSystem().displayMetrics.density).toInt()
}

/*
*   ##########################################################################
*                            GLIDE LIBRARY
*   ##########################################################################
*
* */

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

/*
*   ##########################################################################
*                            ADD PROGRESSBAR
*   ##########################################################################
*
* */

fun Fragment.getProgressbar(activity:Activity,viewGroup:ViewGroup):ProgressBar{
    val progressBar = ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal)
    /*progressBar.layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT)*/
    progressBar.visibility = View.GONE
    progressBar.isIndeterminate = true

    val params = RelativeLayout.LayoutParams(
        RelativeLayout.LayoutParams.MATCH_PARENT,
        RelativeLayout.LayoutParams.MATCH_PARENT
    )

    val rl = RelativeLayout(activity)

    rl.gravity = Gravity.CENTER
    rl.addView(progressBar)

    viewGroup.addView(rl, params)
    return progressBar
}

/*
*   ##########################################################################
*                            TOAST MESSAGE
*   ##########################################################################
*
* */

fun Activity.showMessage(msg:String,duration:Int){
    Toast.makeText(this,msg,duration).show()
}


/*
*   ##########################################################################
*                            FILES -> IMAGES
*   ##########################################################################
*
* */

fun Activity.getFilePathFromIntent(data: Intent):String?{
    var picturePath:String? = null
    val selectedImage: Uri = data.data!!
    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

    val cursor: Cursor? = contentResolver.query(
        selectedImage,
        filePathColumn, null, null, null
    )
    if(cursor!=null){
        cursor.moveToFirst()
        val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
        picturePath = cursor.getString(columnIndex)
        cursor.close()

    }
    return picturePath
}

@SuppressLint("IntentReset")
fun Fragment.selectImageFromGallery(requestCode:Int) {
    val getIntent = Intent(Intent.ACTION_GET_CONTENT)
    getIntent.type = "image/*"

    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    pickIntent.type = "image/*"

    val chooserIntent = Intent.createChooser(getIntent, "Select Image")
    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

    startActivityForResult(
        chooserIntent,
        requestCode
    )

    //val intent = Intent()
    //intent.type = "image/*"
    //intent.action = Intent.ACTION_GET_CONTENT
    //startActivityForResult(
    //Intent.createChooser(
    //intent,
    //"Please select..."
    //),
    //GALLERY_REQUEST_CODE
    //)
}

/*fun View.takeScreenshot():Bitmap{
    val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgDrawable = background
    if(bgDrawable!=null){bgDrawable.draw(canvas)}
    else{canvas.drawColor(Color.WHITE)}
    return bitmap
}*/

fun Activity.downloadImage(item: RunItem, imageView:ImageView){
    val database = Firebase.storage.reference
    val path = "${getImagePath()}${item.downloadUrl}"
    val storageRef = database.child(path)
    loadImageFromStorage(storageRef,imageView)
}

fun Activity.deleteFile(uri: Uri): Boolean {
    val file = File(uri.path!!)
    var selectionArgs = arrayOf<String>(file.getAbsolutePath())
    val contentResolver = contentResolver
    var where: String? = null
    var filesUri: Uri? = null
    if (Build.VERSION.SDK_INT >= 29) {
        filesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        where = MediaStore.Images.Media._ID + "=?"
        selectionArgs = arrayOf(file.getName())
    } else {
        where = MediaStore.MediaColumns.DATA + "=?"
        filesUri = MediaStore.Files.getContentUri("external")
    }
    val result = contentResolver.delete(filesUri, where, selectionArgs)
    return !file.exists()
}

fun Activity.getImageUri(inImage: Bitmap,title:String): Uri? {
    val bytes = ByteArrayOutputStream()
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(contentResolver, inImage, title, null)
    return Uri.parse(path)
}

fun Activity.loadImageFromBitmap(bitmap:Bitmap,imageView:ImageView){
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    GlideApp.with(this)
        .load(bitmap)
        .error(R.drawable.ic_load_error_foreground)
        .circleCrop()
        .override(200, 200)
        .transition(DrawableTransitionOptions.withCrossFade(factory))
        .into(imageView)
}

fun Activity.loadImageFromStorage(storeRef: StorageReference,imageView:ImageView){
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    GlideApp.with(this)
        .load(storeRef)
        .error(R.drawable.ic_load_error_foreground)
        .circleCrop()
        .override(200, 200)
        .transition(DrawableTransitionOptions.withCrossFade(factory))
        .into(imageView)
}

fun Activity.loadImageFromPhone(imagePath:String,imageView:ImageView){
    val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    GlideApp.with(this)
        .load(imagePath)
        .error(R.drawable.ic_load_error_foreground)
        .circleCrop()
        .override(200, 200)
        .transition(DrawableTransitionOptions.withCrossFade(factory))
        .into(imageView)
}

/*
*   ##########################################################################
*                            GPS FUNCTIONS
*   ##########################################################################
*
* */

fun Fragment.getUserLocation():GeoPoint {
    val location: Location?
    if(checkGpsProviderStatus() &&
        ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        location =  (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(location!=null){return GeoPoint(location.latitude,location.longitude)}
    }
    return getCenterOfSweden()
}

fun Fragment.getLocationUpdates(locationListener:LocationListener){
    if(checkGpsProviderStatus() &&
        ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager).requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f,locationListener)
    }
}

fun Fragment.checkGpsProviderStatus():Boolean{
    return (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Activity.getUserLocation():GeoPoint {
    val location: Location?
    if(checkGpsProviderStatus() &&
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        location =  (getSystemService(Context.LOCATION_SERVICE) as LocationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(location!=null){return GeoPoint(location.latitude,location.longitude)}
    }
    return getCenterOfSweden()
}

fun Activity.getLocationUpdates(locationListener:LocationListener){
    if(checkGpsProviderStatus() &&
        ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
        (getSystemService(Context.LOCATION_SERVICE) as LocationManager).requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f,locationListener)
    }
}

fun Activity.checkGpsProviderStatus():Boolean{
    return (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Activity.gpsStatus(){
    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
    moveToActivity(intent)
}

fun getCenterOfHome(): GeoPoint {
    return GeoPoint(59.379108,13.500179)
}

fun getCenterOfSweden(): GeoPoint {
    return GeoPoint(63.167109,15.957184)
}



/*
*   ##########################################################################
*                                JSON
*   ##########################################################################
*
* */

fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
    when (val value = this[it])
    {
        is JSONArray ->
        {
            val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
            JSONObject(map).toMap().values.toList()
        }
        is JSONObject -> value.toMap()
        JSONObject.NULL -> null
        else            -> value
    }
}

/*
*   ##########################################################################
*                                CLEAR CHILDREN
*   ##########################################################################
*
* */

fun ViewGroup.clearChildren(childrenToNotRemove:Int){
    while(childCount>childrenToNotRemove){
        var i = childrenToNotRemove
        val childCount = childCount
        while(i<childCount){
            removeView(getChildAt(i))
            i++
        }
    }
}

/*
*   ##########################################################################
*                                INTENT
*   ##########################################################################
*
* */

fun Activity.moveToActivity(intent:Intent){
    startActivity(intent)
}

/*
*   ##########################################################################
*                                CHECK/UNCHECK CHECKBOXES
*   ##########################################################################
*
* */

fun Fragment.uncheckCheckBoxes(pos:Int,checkBoxes:ArrayList<CheckBox>){
    var i = 0
    while(i<checkBoxes.size){
        if(i!=pos){checkBoxes[i].isChecked = false}
        i++
    }
}

/*
*   ##########################################################################
*                                EDIT TEXTVIEW
*   ##########################################################################
*
* */

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/*
*   ##########################################################################
*                                SELECT FILE FROM DEVICE
*   ##########################################################################
*
* */

/*@Deprecated("Deprecated in Java")
private val GALLERY_REQUEST_CODE = 102
private val PICK_IMAGE = 1
private var fileUri: Uri? = null
private var filePath:String? = null
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PICK_IMAGE
        && resultCode == Activity.RESULT_OK
        && data != null
        && data.data != null
    ) {
        filePath = parentActivity.getFilePathFromIntent(data)
        fileUri = data.data!!
        //parentActivity.loadImageFromPhone(fileUri.toString(),binding.imageView)
    }
}*/