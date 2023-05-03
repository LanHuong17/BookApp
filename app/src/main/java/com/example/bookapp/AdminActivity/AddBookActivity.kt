package com.example.bookapp.AdminActivity

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.Model.ModelCategory
import com.example.bookapp.databinding.ActivityAddBookBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist hold categories's pdf
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //uri of picked pdf
    private var pdfUri: Uri? = null

    //tag
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Pleaser wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadPdfCategory()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addBook.setOnClickListener {
            validateData()
        }

        binding.addPdfBtn.setOnClickListener {
            pickPdfIntent()
        }

        binding.pickCate.setOnClickListener {
            categoryPickDialog()
        }
    }
    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        //get data
        title = binding.inputTitle.text.toString().trim()
        description = binding.inputDescript.text.toString().trim()
        category = binding.pickCate.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Pls enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Pls enter description", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Pls pick category", Toast.LENGTH_SHORT).show()
        } else if(pdfUri == null) {
            Toast.makeText(this, "Pls pick pdf", Toast.LENGTH_SHORT).show()
        } else {
            uploadPdfToStorage()
        }
    }


    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage")

        progressDialog.setMessage("Uploading pdf")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()
        val filePathAndName = "Books/$timestamp"

        val storeReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storeReference.putFile(pdfUri!!)

            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "LoadPdfCategories: Pdf uploading")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }

            .addOnFailureListener { e->
                Log.d(TAG, "LoadPdfCategories: Failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: Loading to db")
        progressDialog.setMessage("Uploading pdf info")

        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = "$timestamp"
//        hashMap["viewcount"] = 0
//        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "LoadPdfCategories: Uploaded")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {
                e->
                Log.d(TAG, "uploadPdfInfoToDb: Failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadPdfCategory() {
        Log.d(TAG, "LoadPdfCategories: Loading pdf categories")
         //init arraylist
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of cate from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category").setItems(categoriesArray) {
            dialog, which ->
            //get clicked item
            selectedCategoryTitle = categoryArrayList[which].category
            selectedCategoryId = categoryArrayList[which].id
            //set category to textview
            binding.pickCate.text = selectedCategoryTitle

            Log.d(TAG, "categoryPickDialog: Selected category: $selectedCategoryTitle")
        }.show()
    }

    private fun pickPdfIntent() {
        Log.d(TAG, "pickPdfIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF Pick cancelled")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

}