package com.example.runadvisor.database
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.runadvisor.enums.ServerResult
import com.example.runadvisor.io.printToTerminal
import com.example.runadvisor.struct.RunItem
import com.example.runadvisor.struct.ServerDetails
import com.example.runadvisor.struct.UserItem
import kotlinx.coroutines.tasks.await

class FirestoreViewModel:ViewModel() {
    var serverDetails = ArrayList<ServerDetails>()
    var firebaseRepository = FirestoreRepository()
    var savedRunItems : MutableLiveData<List<RunItem>?> = MutableLiveData()

    fun clearServerDetails(){
        serverDetails.clear()
    }

    suspend fun savePublicRunItemToFirebase(pos:Int,runItem: RunItem):Boolean{
        var result = true
        firebaseRepository.savePublicRunItem(runItem).addOnCompleteListener { task->
            if(!task.isSuccessful){
                serverDetails.add(ServerDetails(pos,task.exception.toString(), ServerResult.UPLOAD_ERROR))
                result = false
            }
        }.await()
        return result
    }

    suspend fun saveUserRunItemToFirebase(pos:Int,userItem:UserItem):Boolean{
        var result = true
        firebaseRepository.saveUserRunItem(userItem).addOnCompleteListener { task->
            if(!task.isSuccessful){
                serverDetails.add(ServerDetails(pos,task.exception.toString(), ServerResult.UPLOAD_ERROR))
                result = false
            }
        }.await()
        return result
    }

    suspend fun saveImageToFirebase(pos:Int,imageUri: Uri,downloadUrl:String):Boolean{
        var result = true
        firebaseRepository.saveImage(imageUri, downloadUrl).addOnCompleteListener { task->
            if(!task.isSuccessful){
                serverDetails.add(ServerDetails(pos,task.exception.toString(), ServerResult.UPLOAD_ERROR))
                result = false
            }
        }.await()
        return result
    }

    fun getRunItems(): LiveData<List<RunItem>?> {
        firebaseRepository.getSavedPublicRunItems().addSnapshotListener EventListener@{ value, e ->
            if (e != null) {
                printToTerminal("Listen failed ${e.message.toString()}")
                savedRunItems.value = null
                return@EventListener
            }

            val savedRunItemList : MutableList<RunItem> = mutableListOf()
            /*for (doc in value!!) {
                val runItem = doc.toObject(RunItem::class.java)
                savedRunItemList.add(runItem)
            }*/
            for (doc in value!!.documentChanges) {
                val runItem = doc.document.toObject(RunItem::class.java)
                savedRunItemList.add(runItem)
            }
            savedRunItems.value = savedRunItemList
        }

        return savedRunItems
    }

    fun deletePublicRunItem(runItem:RunItem){
        firebaseRepository.deletePublicRunItem(runItem).addOnFailureListener {
            printToTerminal("Failed to delete PublicRunItem")
        }
    }

    fun deleteUserRunItem(userItem:UserItem){
        firebaseRepository.deleteUserRunItem(userItem).addOnFailureListener {
            printToTerminal("Failed to delete UserRunItem")
        }
    }

    fun deleteImage(runItem:RunItem){
        firebaseRepository.deleteImage(runItem).addOnFailureListener {
            printToTerminal("Failed to delete Image")
        }
    }



    /*
    GET SUBCOLLECTION
    documentIdList:ArrayList<String>
    var i = 0
    while(i<documentIdList.size){
        Firebase.firestore.collection(getPublicRunItemsCollection())
            .document(documentIdList.get(i))
            .collection(getItemCollection())
            .get()
            .addOnSuccessListener{ documentSnapShot ->
                for(document in documentSnapShot.documents){
                    val runItem = document.toObject<RunItem>()
                    if(runItem!=null){
                        customAdapter.serverData.add(runItem)
                        customAdapter.notifyItemInserted(customAdapter.serverData.size-1)
                    }
                }
            }
        i++
    }*/
    /*
     IF documentExist(docId:String):Boolean{
        var result = false
        val eventsRef: CollectionReference = Firebase.firestore.collection(getUserRunItemsCollection())
        val docIdQuery: Query = eventsRef.whereEqualTo("docId", docId)
        docIdQuery.get().addOnCompleteListener{task->
            result = task.result.isEmpty
        }.await()
        return !result
    * */

    /*
        removeDocument(docId:String){
        val eventsRef: CollectionReference = Firebase.firestore.collection(getUserRunItemsCollection())
        val docIdQuery: Query = eventsRef.whereEqualTo("docId", docId)
        docIdQuery.get().addOnCompleteListener{task->
                if(task.isSuccessful){
                    for (document in task.result){
                        document.reference.delete().addOnSuccessListener{
                            printToTerminal("Document successfully deleted!")
                        }.addOnFailureListener{
                            printToTerminal("Error deleting document ${it.message.toString()}")}
                    }
                }
                else{printToTerminal("Error getting documents: ${task.exception}")}
            }.await()

    }
    * */
}