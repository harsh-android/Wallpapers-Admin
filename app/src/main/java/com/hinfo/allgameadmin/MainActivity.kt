package com.hinfo.allgameadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hinfo.allgameadmin.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var storageRef: StorageReference
    lateinit var dbRef: DatabaseReference
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference


        binding.btnUpload.setOnClickListener {
            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == 25) {
                var uri = data?.data
                Log.e(TAG, "onActivityResult: ========= " + uri.toString())
                var formate = SimpleDateFormat("ddMMyyyy-hhmss")
                var current = formate.format(Date())

                val ref = storageRef.child("images/${current}.jpg")
                var uploadTask = ref.putFile(uri!!)

                val urlTask = uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    ref.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result

                        var key = dbRef.root.push().key
                        var data = ImageModel(key!!, downloadUri.toString())
                        dbRef.root.child("Images").child(key).setValue(data)

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }
        }

    }

}