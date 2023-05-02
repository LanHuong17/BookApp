package com.example.bookapp.AdminActivity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityAddChapterBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AddChapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddChapterBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var bookArrayList: ArrayList<ModelBook>
    private var pdfUri: Uri? = null
    private var TAG = "CHAPTER_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAddChapterBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Pleaser wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadPdfBook()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addChapter.setOnClickListener {
            validateData()
//            Log.d(TAG, "Book Title: ${selectedBookTitle}, Chapter: ${title}, Date: $date")
        }

        binding.addPdfBtn.setOnClickListener {
            pickPdfIntent()
        }

        binding.pickBook.setOnClickListener {
            categoryPickDialog()
        }
    }


    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Showing pdf category pick dialog")

        //get string array of cate from arraylist
        val booksArray = arrayOfNulls<String>(bookArrayList.size)
        for (i in bookArrayList.indices) {
            booksArray[i] = bookArrayList[i].title
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Book").setItems(booksArray) {
                dialog, which ->
            //get clicked item
            selectedBookTitle = bookArrayList[which].title
            selectedBookId = bookArrayList[which].id
            //set category to textview
            binding.pickBook.text = selectedBookTitle

            Log.d(TAG, "categoryPickDialog: Selected category: $selectedBookTitle")
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

    private var title = ""
    private var book = ""

    private var selectedBookId = ""
    private var selectedBookTitle = ""

    private var isFavorite = false

    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        title = binding.inputTitle.text.toString().trim()
        book = binding.pickBook.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Pls enter title", Toast.LENGTH_SHORT).show()
        } else if (book.isEmpty()) {
            Toast.makeText(this, "Pls pick book", Toast.LENGTH_SHORT).show()
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
        val filePathAndName = "Chapters/$timestamp"

        val storeRef = FirebaseStorage.getInstance().getReference(filePathAndName)
        storeRef.putFile(pdfUri!!)

            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: Pdf uploading")
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
                makeNotify(uploadedPdfUrl, timestamp)
                checkFavorite()

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: Failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: Loading to db")
        progressDialog.setMessage("Uploading pdf info")

        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap["bookId"] = "$selectedBookId"
        hashMap["id"] = "$timestamp"
        hashMap["timestamp"] = "$timestamp"
        hashMap["uid"] = "$uid"
        hashMap["titleBook"] = "$selectedBookTitle"
        hashMap["titleChapter"] = "$title"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["viewCount"] = 0
        hashMap["downloadCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: Uploaded")
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

    private fun loadPdfBook() {
        Log.d(TAG, "loadPdfBook: Loading books")
        //init arraylist
        bookArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                bookArrayList.clear()
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelBook::class.java)
                    bookArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.title}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun makeNotify(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "makeNotify: Loading to db")
        progressDialog.setMessage("Uploading pdf info")

        val uid = firebaseAuth.uid
        val hashMap: HashMap<String, Any> = HashMap()

        hashMap["bookId"] = "$selectedBookId"
        hashMap["id"] = "$timestamp"
        hashMap["timestamp"] = "$timestamp"
        hashMap["uid"] = "$uid"
        hashMap["titleBook"] = "$selectedBookTitle"
        hashMap["titleChapter"] = "$title"
        hashMap["url"] = "$uploadedPdfUrl"

        val ref = FirebaseDatabase.getInstance().getReference("Notifies")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "makeNotify: Uploaded")
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener {
                    e->
                Log.d(TAG, "makeNotify: Failed due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(selectedBookId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    if (isFavorite) {
//                        sendNotification(this@AddChapterActivity, "BookApp","$selectedBookTitle has new chapter")
                    } else {
                        //...
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}