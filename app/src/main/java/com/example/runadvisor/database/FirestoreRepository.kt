package com.example.runadvisor.database
import android.net.Uri
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.UserItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

class FirestoreRepository{
    private val firestoreDB = FirebaseFirestore.getInstance()
    private val firestoreStorage = Firebase.storage.reference
    private val user = FirebaseAuth.getInstance().currentUser


    fun validUser():Boolean{return user!=null}

    fun validImageUri():Boolean{
        return false
    }

    fun saveUserRunItem(userItem: UserItem): Task<Void> {
        val documentReference = firestoreDB.collection(getUserCollection())
            .document(user!!.email.toString())
            .collection(getItemCollection())
            .document(userItem.docId)
        return documentReference.set(userItem)
    }

    fun savePublicRunItem(runItem: RunItem): Task<Void> {
        return firestoreDB.collection(getItemCollection())
            .document(runItem.docID!!)
            .set(runItem)
    }

    fun saveImage(imageUri: Uri, downloadUrl:String):UploadTask{
        val path = "${getImagePath()}${downloadUrl}"
        val storageRef = firestoreStorage.child(path)
        return storageRef.putFile(imageUri)
    }

    fun getSavedUserRunItems(): CollectionReference {
        val path = "${getUserCollection()}/${user!!.email.toString()}/${getItemCollection()}"
        return firestoreDB.collection(path)
    }

    fun getSavedPublicRunItems(): CollectionReference {
        return firestoreDB.collection(getItemCollection())
    }

    fun deleteUserRunItem(userItem: UserItem): Task<Void> {
        val path = "${getUserCollection()}/${user!!.email.toString()}/${getItemCollection()}"
        return firestoreDB.collection(path).document(userItem.docId).delete()
    }

    fun deletePublicRunItem(runItem: RunItem): Task<Void> {
        return firestoreDB.collection(getItemCollection()).document(runItem.docID!!).delete()
    }

    fun deleteImage(runItem: RunItem): Task<Void> {
        val path = "${getImagePath()}${runItem.downloadUrl}"
        val storageRef = firestoreStorage.child(path)
        return storageRef.delete()
    }
}