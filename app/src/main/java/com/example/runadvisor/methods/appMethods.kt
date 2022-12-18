package com.example.runadvisor.methods
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

fun getScreenWidth() : Int{
    return Resources.getSystem().displayMetrics.widthPixels
}

fun getScreenHeight() : Int{
    return Resources.getSystem().displayMetrics.heightPixels
}

fun convertDpToPixel(value : Int):Int{
    return (value* Resources.getSystem().displayMetrics.density).toInt()
}

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

/*fun View.takeScreenshot():Bitmap{
    val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgDrawable = background
    if(bgDrawable!=null){bgDrawable.draw(canvas)}
    else{canvas.drawColor(Color.WHITE)}
    return bitmap
}*/

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

fun Activity.showMessage(msg:String,duration:Int){
    Toast.makeText(this,msg,duration).show()
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

@SuppressLint("InternalInsetResource")
fun Activity.getTitleBarHeight():Int{
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if(resourceId>0){
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun Double.format(digits: Int) = "%.${digits}f".format(this)

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