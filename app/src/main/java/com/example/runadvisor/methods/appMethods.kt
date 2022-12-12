package com.example.runadvisor.methods
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.runadvisor.R
import com.example.runadvisor.struct.PublicRunItem
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.InputStream

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

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

@SuppressLint("IntentReset")
//https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
// TO TAKE PHOTO INSTEAD
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

fun Activity.moveToActivity(intent:Intent){
    startActivity(intent)
}

fun Activity.downloadImage(item: PublicRunItem,imageView:ImageView){
    val database = Firebase.storage.reference
    val path = "${getImagePath()}${item.downloadUrl}"
    val storageRef = database.child(path)
    loadImageFromStorage(storageRef,imageView)
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

fun Activity.removeActionBarHeight():Float{
    val styledAttributes = baseContext.theme.obtainStyledAttributes(
        intArrayOf(android.R.attr.actionBarSize)
    )
    val mActionBarSize = styledAttributes.getDimension(0,0.0f)
    styledAttributes.recycle()
    return mActionBarSize
}

fun Activity.gpsStatus(){
    val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
    moveToActivity(intent)
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
