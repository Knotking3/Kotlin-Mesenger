package com.example.kotlinmessenger.registerlogin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.messages.LatestMessegesActivity
import com.google.firebase.auth.FirebaseAuth
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.title = "Login"


        login_button_login.setOnClickListener {
            performLogin()

        }
        backtoregister_login.setOnClickListener {
            finish()
        }

    }
    private fun performLogin(){
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "please enter tet in email/pw", Toast.LENGTH_SHORT).show()
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "You Logged in: ${it.result?.user?.uid}", Toast.LENGTH_SHORT).show()

                //Send info to OneSignal
                val uid = FirebaseAuth.getInstance().uid ?: ""
                val LoggedIn_User_UID: String? = uid
                Toast.makeText(this, "Current User is $LoggedIn_User_UID", Toast.LENGTH_SHORT).show()

                OneSignal.sendTag("User_ID", LoggedIn_User_UID)
                Toast.makeText(this, "Sent $LoggedIn_User_UID to oneSignial", Toast.LENGTH_SHORT).show()
                //One Signial

                    val intent = Intent(this, LatestMessegesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to Log in:  ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}