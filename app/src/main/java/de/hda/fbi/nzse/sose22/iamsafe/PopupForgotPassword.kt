package de.hda.fbi.nzse.sose22.iamsafe


import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PopupForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_forgot_password)

        val sendMailButton: android.widget.Button = findViewById(R.id.button_forgotpassword_sendmail)

        sendMailButton.setOnClickListener {
            val emailAddress = findViewById<EditText>(R.id.textinput_forgotpassword_emailaddress).text.toString()
            Firebase.auth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(this, "Email sent - check spam as well!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    }

                    Intent(this, LoginActivity::class.java).also { intent = it }
                    startActivity(intent)
                }
        }
        val backButton: android.widget.Button = findViewById(R.id.button_forgotpassword_back)

        backButton.setOnClickListener {
            Intent(this, LoginActivity::class.java).also { intent = it}
            startActivity(intent)
        }
    }

}