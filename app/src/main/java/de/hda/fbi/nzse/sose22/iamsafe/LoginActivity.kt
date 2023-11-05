package de.hda.fbi.nzse.sose22.iamsafe

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Disable Dark Mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Initialize Firebase Auth
        auth = Firebase.auth

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#E7F8F5")))
        val loginButton: android.widget.Button = findViewById(R.id.button_login_login)


        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.textinput_login_emailaddress).text.toString()
            val password = findViewById<EditText>(R.id.textinput_login_password).text.toString()
            if (!email.contains('@')) {
                    Toast.makeText(this, "Invalid Email address!",
                        Toast.LENGTH_SHORT).show()
            } else {
                signIn(email, password)
            }

        }

        val forgotPwText = findViewById<View>(R.id.clickable_textview_login_forgotpassword) as TextView

        forgotPwText.setOnClickListener {
            val intent = Intent(this, PopupForgotPassword::class.java)
            startActivity(intent)
        }
    }

    public override fun onStart() {
        super.onStart()

        Firebase.dynamicLinks.getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                val deepLink: Uri?
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    if(deepLink?.pathSegments != null && deepLink.pathSegments[0] == "addContact" && deepLink.pathSegments[1] != null) {
                        DataStore.addContactIdOnLaunch = deepLink.pathSegments[1]
                    }
                }
            }

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            DataStore.userId = currentUser.uid
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra("currentUser", currentUser)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    DataStore.userId = user!!.uid
                    intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("currentUser", user)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    reload()
                }
            }
    }



    private fun reload(){
        finish()
        startActivity(this.intent)
    }



}

