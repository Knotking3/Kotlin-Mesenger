package com.example.kotlinmessenger.other

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.kotlinmessenger.R
import com.example.kotlinmessenger.messages.LatestMessegesActivity
import com.example.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.activity_profile_settings.*
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class ProfileSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)
        supportActionBar?.title = "Profile Settings"

        update_account_button_settings.setOnClickListener {
            updateAccount()
        }

        profile_picture_settings.setOnClickListener {
            Toast.makeText(this, "Try to show photo selecter", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Toast.makeText(this, "Photo was selected", Toast.LENGTH_SHORT).show()

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_settings.setImageBitmap(bitmap)

            profile_picture_settings.alpha = 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //profile_picture_register.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun updateAccount() {
//        val email = email_edittext_register.text.toString()
//        val password = password_edittext_register.text.toString()

        if (name_edittext_settings == null  || selectedPhotoUri == null){
            Toast.makeText(this, "please fill out all of the info above", Toast.LENGTH_SHORT).show()

        }
        uploadImageToFirebaseStorage()



    }

    private fun uploadImageToFirebaseStorage() {

        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Toast.makeText(this, "Succesfully uploaded image ${it.metadata?.path}", Toast.LENGTH_SHORT).show()

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Toast.makeText(this, "File Location: $it", Toast.LENGTH_SHORT).show()

                    saveUserToFirebaseDatabase(it.toString())

                }
            }

    }
    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, name_edittext_settings.text.toString(), profileImageUrl)


        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Finally we saved the user to Firebase Database", Toast.LENGTH_SHORT).show()

            }
    }

}