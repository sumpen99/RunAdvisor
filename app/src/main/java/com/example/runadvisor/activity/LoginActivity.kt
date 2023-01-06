package com.example.runadvisor.activity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.runadvisor.MainActivity
import com.example.runadvisor.R
import com.example.runadvisor.methods.authErrors
import com.example.runadvisor.methods.hideKeyboard
import com.example.runadvisor.methods.moveToActivity
import com.example.runadvisor.methods.showMessage
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var emailField:EditText
    private lateinit var passwordField:EditText
    private lateinit var onBackPressedCallback:OnBackPressedCallback

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setEventListener(findViewById(R.id.mainLoginView))
        setOnBackNavigation()
    }

    private fun setOnBackNavigation(){
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed(){}
        }
        onBackPressedDispatcher.addCallback(this,onBackPressedCallback)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEventListener(view:View){
        emailField = findViewById(R.id.userEmail)
        passwordField = findViewById(R.id.userPassword)
        val logInBtn:Button = findViewById(R.id.logInBtn)
        val signUpBtn:Button = findViewById(R.id.signUpBtn)

        logInBtn.setOnClickListener{logIn()}
        signUpBtn.setOnClickListener{signUp()}

        view.setOnTouchListener { v, event ->
            when(event.actionMasked){
                MotionEvent.ACTION_DOWN -> {emailField.hideKeyboard();passwordField.hideKeyboard()}
            }
            true
        }

    }

    private fun logIn(){
        if( illegalUserInput()){return}
        Firebase.auth.signInWithEmailAndPassword(emailField.text.toString(),passwordField.text.toString())
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){enterMain()}
                else{showUserException(task)}
            }
    }

    private fun signUp() {
        if( illegalUserInput()){return}
        Firebase.auth.createUserWithEmailAndPassword(emailField.text.toString(),passwordField.text.toString())
            .addOnCompleteListener { task ->
                if(task.isSuccessful){enterMain()}
                else{showUserException(task)}
            }
    }

    private fun illegalUserInput():Boolean{
        return (emailField.text.toString().isEmpty() || passwordField.text.toString().isEmpty())
    }

    private fun showUserException(task: Task<AuthResult>){
        val errorMessage = try{
            val errorCode = (task.exception as FirebaseAuthException).errorCode
            authErrors[errorCode] ?: R.string.error_login_default_error
        } catch(err:Exception){
            R.string.error_login_default_error
        }
        showMessage(getString(errorMessage),Toast.LENGTH_SHORT)
    }

    private fun enterMain(){
        moveToActivity(Intent(this,MainActivity::class.java))
    }
}