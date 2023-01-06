package com.example.runadvisor.database
import android.net.Uri
import com.example.runadvisor.methods.*
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.UserItem
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
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
        val documentReference = firestoreDB.collection(USER_COLLECTION)
            .document(user!!.email.toString())
            .collection(ITEM_COLLECTION)
            .document(userItem.docId!!)
        return documentReference.set(userItem)
    }

    fun savePublicRunItem(runItem: RunItem): Task<Void> {
        return firestoreDB.collection(ITEM_COLLECTION)
            .document(runItem.docID!!)
            .set(runItem)
    }

    fun saveImage(imageUri: Uri, downloadUrl:String):UploadTask{
        val path = "$IMAGE_PATH${downloadUrl}"
        val storageRef = firestoreStorage.child(path)
        return storageRef.putFile(imageUri)
    }

    fun getSavedUserRunItems(): CollectionReference {
        val path = "$USER_COLLECTION/${user!!.email.toString()}/$ITEM_COLLECTION"
        return firestoreDB.collection(path)
    }

    fun getSavedPublicRunItems(): CollectionReference {
        return firestoreDB.collection(ITEM_COLLECTION)
    }

    fun getSavedPublicRunItem(docId:String?): Query{
        val eventsRef: CollectionReference = firestoreDB.collection(ITEM_COLLECTION)
        val docIdQuery: Query = eventsRef.whereEqualTo("docID", docId)
        return docIdQuery
        //return firestoreDB.collection(getItemCollection()).document(docId!!)
    }

    fun getImageStorageReference(downloadUrl:String?):StorageReference{
        val path = "$IMAGE_PATH${downloadUrl}"
        return firestoreStorage.child(path)
    }

    fun deleteUserRunItem(docId: String): Task<Void> {
        val path = "$USER_COLLECTION/${user!!.email.toString()}/$ITEM_COLLECTION"
        return firestoreDB.collection(path).document(docId).delete()
    }

    fun deletePublicRunItem(docId: String): Task<Void> {
        return firestoreDB.collection(ITEM_COLLECTION).document(docId).delete()
    }

    fun deleteImage(downloadUrl:String): Task<Void> {
        val path = "$IMAGE_PATH${downloadUrl}"
        val storageRef = firestoreStorage.child(path)
        return storageRef.delete()
    }
}