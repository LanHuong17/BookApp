package com.example.bookapp

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Display.Mode
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterChapterAdmin
import com.example.bookapp.Func.Constants
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Func.MyApplication.Companion.incrementDownloadCount
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.Model.ModelChapter
import com.example.bookapp.Model.ModelComment
import com.example.bookapp.databinding.ActivityReadBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_read_book.*
import java.io.FileOutputStream
import java.security.cert.Extension

class ReadBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadBookBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var chapterArrayList: ArrayList<ModelChapter>
    private lateinit var spChapter: Spinner

    var chapterId = ""
    var bookId = ""

    var chapterTitle = ""
    var chapterUrl = ""

    private companion object {
        const val TAG = "CHAPTER_DETAIL_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReadBookBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        chapterId = intent.getStringExtra("chapterId")!!
        bookId = intent.getStringExtra("bookId")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.downloadBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
             == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION granted")

                downloadChapter()

            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is not granted")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        MyApplication.incrementChapterView(chapterId)

        loadChapterDetail()
        loadChapterList()
    }

    private var requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "onCreate: STORAGE PERMISSION granted")
            downloadChapter()
        } else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION denied")
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadChapter() {
        Log.d(TAG, "downloadChapter: Chapter downloading")
        progressDialog.setMessage("Downloading Chapter")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(chapterUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)

            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadChapter: Chapter downloaded")
                saveToDownloadFolder(bytes)
            }

            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadChapter: Download failed ${e.message}")
                Toast.makeText(this, "downloadChapter: Download failed ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadFolder: Saving downloaded book")
        val nameWithExtension = "$chapterTitle.pdf"
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsFolder.mkdirs()

        val filePath= downloadsFolder.path +"/"+ nameWithExtension
        val out = FileOutputStream(filePath)
        out.write(bytes)
        out.close()

        Toast.makeText(this, "Saved to download folder", Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
        incrementDownloadCount(chapterId)
        try {

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadFolder: Save failed ${e.message}")
            Toast.makeText(this, "Save failed ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadChapterDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.child(chapterId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    chapterUrl = "${snapshot.child("url").value}"
                    chapterTitle = "${snapshot.child("titleChapter").value}"

                    binding.tvTitleChapter.text = chapterTitle

                    Log.d(TAG, "onDataChange: PDF_URL: $chapterUrl")
                    Log.d(TAG, "onDataChange: TitleChapter: $chapterTitle")

                    loadChapterPdf("$chapterUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var currentPage: Int = 0

    private fun loadChapterPdf(chapterUrl: String) {
        Log.d(TAG, "loadChapterPdf: get pdf from fb")

        val refPdf = FirebaseStorage.getInstance().getReferenceFromUrl(chapterUrl)
        refPdf.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "loadChapterPdf: $chapterUrl")

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        currentPage = page + 1


                        binding.tvSubTitle.text = "$currentPage/$pageCount"
                        Log.d(TAG, "onDataChange: $currentPage/$pageCount")
                    }
                    .onError {
                        t -> Log.d(TAG, "loadChapterPdf: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadChapterPdf: ${t.message}")
                    }
                    .load()

                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener { f ->
                Log.d(TAG, "loadChapterPdf: ${f.message}")
                binding.progressBar.visibility = View.GONE
            }
    }

    var selectedChapterId = ""
    var selectedChapterTitle = ""
    val chapterTitles = ArrayList<String>()
    val chapterIds = ArrayList<String>()

    private fun loadChapterList() {
        chapterArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.orderByChild("bookId").equalTo(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    chapterArrayList.clear()
                    //get data
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelChapter::class.java)
                        //add to list
                        if (model != null) {
                            chapterArrayList.add(model)
                        }
                    }
                    // create a new list with titleChapter and chapterId
                    for (chapter in chapterArrayList) {
                        chapterTitles.add(chapter.titleChapter)
                        chapterIds.add(chapter.id)
                    }

                    // set up ArrayAdapter for the Spinner
                    val adapter = ArrayAdapter(this@ReadBookActivity, android.R.layout.simple_spinner_item, chapterTitles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    spChapter = binding.spChapter
//                    spChapter.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }



}