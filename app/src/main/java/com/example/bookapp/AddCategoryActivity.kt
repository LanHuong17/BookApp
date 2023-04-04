package com.example.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityAddCategoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCategoryActivity : AppCompatActivity() {

    private var category = ""

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)

        progressDialog.setTitle("Pleaser wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addCateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        category = binding.inputCate.text.toString().trim()

        if (category.isEmpty()) {
            Toast.makeText(this, "Enter category", Toast.LENGTH_SHORT).show()
        } else {
            addCategory()
        }
    }

    private fun addCategory() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp").setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed add category due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}