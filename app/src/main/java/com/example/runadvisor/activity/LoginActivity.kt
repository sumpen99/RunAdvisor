package com.example.runadvisor.activity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.hideKeyboard
import com.example.runadvisor.methods.moveToActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var emailField:EditText
    private lateinit var passwordField:EditText
    private lateinit var logInBtn:Button
    private lateinit var signUpBtn:Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setEventListener(findViewById(R.id.mainLoginView))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view:View){
        auth = Firebase.auth
        emailField = findViewById(R.id.userEmail)
        passwordField = findViewById(R.id.userPassword)
        logInBtn = findViewById(R.id.logInBtn)
        signUpBtn = findViewById(R.id.signUpBtn)

        logInBtn.setOnClickListener{LogIn()}
        signUpBtn.setOnClickListener{signUp()}

        view.setOnTouchListener { v, event ->
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {emailField.hideKeyboard();passwordField.hideKeyboard()}
                //MotionEvent.ACTION_POINTER_DOWN -> {}
                //MotionEvent.ACTION_MOVE -> {}
                //MotionEvent.ACTION_UP -> {}
                //MotionEvent.ACTION_POINTER_UP -> {}
                //MotionEvent.ACTION_CANCEL -> {}
            }
            true
        }

    }

    private fun LogIn(){
        if( illegalUserInput()){return}
        auth.signInWithEmailAndPassword(emailField.text.toString(),passwordField.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    moveToActivity(Intent(this,HomeActivity::class.java))
                } else {
                    Toast.makeText(baseContext, "Authentication failed ${task.exception}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signUp() {
        if( illegalUserInput()){return}
        auth.createUserWithEmailAndPassword(emailField.text.toString(),passwordField.text.toString())
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    moveToActivity(Intent(this,HomeActivity::class.java))
                } else {
                    Toast.makeText(baseContext, "${task.exception}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initUserCollection(){
        val user = auth.currentUser
        if(user!=null){
            //Firebase.firestore.collection("Users").add("",User(user.email.toString()))
            //Firebase.firestore.collection("Users").document(user.uid).set(User(user.email.toString()))
        }
    }

    private fun illegalUserInput():Boolean{
        return (emailField.text.toString().isEmpty() || passwordField.text.toString().isEmpty())
    }
}