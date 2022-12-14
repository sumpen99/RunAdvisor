package com.example.runadvisor
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runadvisor.activity.HomeActivity
import com.example.runadvisor.activity.LoginActivity
import com.example.runadvisor.methods.moveToActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isUserActive()
    }

    private fun isUserActive(){
        if(Firebase.auth.currentUser!=null){
            //Firebase.auth.signOut()
            //printToTerminal(Firebase.auth.currentUser.toString())
            moveToActivity(Intent(this, HomeActivity()::class.java))
        }
        else{
            moveToActivity(Intent(this,LoginActivity::class.java))
        }
    }

}