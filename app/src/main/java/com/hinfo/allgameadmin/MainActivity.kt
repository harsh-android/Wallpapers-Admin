package com.hinfo.allgameadmin

import android.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    var cateList = ArrayList<CategoryModel>()
    var cate = ArrayList<String>()

    var selCate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbRef = FirebaseDatabase.getInstance().reference
        storageRef = FirebaseStorage.getInstance().reference

        dbRef.root.child("Category").addValueEventListener(object : ValueEventListener {
            @SuppressLint("ResourceAsColor")
            override fun onDataChange(snapshot: DataSnapshot) {
                cate.clear()
                cateList.clear()
                for (data in snapshot.children) {
                    var model = data.getValue(CategoryModel::class.java)
                    cateList.add(model!!)
                    cate.add(model.name)
                }

                var adapter = ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,cate)
                binding.spinCate.adapter = adapter

                binding.spinCate.onItemSelectedListener = object :OnItemSelectedListener{
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selCate = cate.get(position)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.btnUpload.setOnClickListener {
            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        binding.btnAddCategory.setOnClickListener {

            var text = binding.edtCate.text.toString()

            if (text.isNotEmpty()) {

                var key = dbRef.root.push().key

                var data = CategoryModel(key!!,text)
                dbRef.root.child("Category").child(key).setValue(data)
            }

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

                        var key = dbRef.root.child("Images").push().key
                        var data = ImageModel(key!!, selCate, downloadUri.toString())
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