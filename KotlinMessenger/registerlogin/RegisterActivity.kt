package com.example.kotlinmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        supportActionBar?.title = "Create Account"


        register_button_register.setOnClickListener {
            performRegister()
        }
        already_have_account_register.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        profile_picture_register.setOnClickListener {
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
            Toast.makeText(this, "URI is $selectedPhotoUri", Toast.LENGTH_SHORT).show()




            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)


            selectphoto_imageview_register.setImageBitmap(compressBitmap(bitmap, 2))

            profile_picture_register.alpha = 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //profile_picture_register.setBackgroundDrawable(bitmapDrawable)

        }
    }
    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (name_edittext_register == null || email.isEmpty() || password.isEmpty() || selectedPhotoUri == null){
            Toast.makeText(this, "please fill out all of the info above", Toast.LENGTH_SHORT).show()
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener
                Toast.makeText(this, "Succesfully created user with uid", Toast.LENGTH_SHORT).show()

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to create User", Toast.LENGTH_SHORT).show()
            }

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

        val user = User(uid, name_edittext_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Finally we saved the user to Firebase Database", Toast.LENGTH_SHORT).show()

                //Send info to OneSignal
                val LoggedIn_User_UID: String? = uid
                Toast.makeText(this, "Current User is $LoggedIn_User_UID", Toast.LENGTH_SHORT).show()

                OneSignal.sendTag("User_ID", LoggedIn_User_UID)
                Toast.makeText(this, "Sent $LoggedIn_User_UID to oneSignial", Toast.LENGTH_SHORT).show()
                //OneSignal


                val intent = Intent(this, LatestMessegesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
    }

    private fun compressBitmap(bitmap:Bitmap, quality:Int):Bitmap{
        // Initialize a new ByteArrayStream
        val stream = ByteArrayOutputStream()

        /*
            **** reference source developer.android.com ***

            public boolean compress (Bitmap.CompressFormat format, int quality, OutputStream stream)
                Write a compressed version of the bitmap to the specified outputstream.
                If this returns true, the bitmap can be reconstructed by passing a
                corresponding inputstream to BitmapFactory.decodeStream().

                Note: not all Formats support all bitmap configs directly, so it is possible
                that the returned bitmap from BitmapFactory could be in a different bitdepth,
                and/or may have lost per-pixel alpha (e.g. JPEG only supports opaque pixels).

                Parameters
                format : The format of the compressed image
                quality : Hint to the compressor, 0-100. 0 meaning compress for small size,
                    100 meaning compress for max quality. Some formats,
                    like PNG which is lossless, will ignore the quality setting
                stream: The outputstream to write the compressed data.

                Returns
                    true if successfully compressed to the specified stream.


            Bitmap.CompressFormat
                Specifies the known formats a bitmap can be compressed into.

                    Bitmap.CompressFormat  JPEG
                    Bitmap.CompressFormat  PNG
                    Bitmap.CompressFormat  WEBP
        */

        // Compress the bitmap with JPEG format and quality 50%
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        val byteArray = stream.toByteArray()

        // Finally, return the compressed bitmap
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
