package com.example.bookapp

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.bookapp.Func.Constants
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Func.MyApplication.Companion.incrementDownloadCount
import com.example.bookapp.databinding.ActivityReadBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.io.IOException
import java.security.cert.Extension

class ReadBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadBookBinding
    private lateinit var progressDialog: ProgressDialog
    var media: MediaPlayer? = null

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

        MyApplication.incrementChapterView(chapterId, bookId)

        loadChapterDetail()

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
        incrementDownloadCount(chapterId, bookId)
        try {

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadFolder: Save failed ${e.message}")
            Toast.makeText(this, "Save failed ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun loadChapterDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Chapters").child(chapterId)
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

    private fun loadChapterPdf(chapterUrl: String) {
        Log.d(TAG, "loadChapterPdf: get pdf from fb")


        val refPdf = FirebaseStorage.getInstance().getReferenceFromUrl(chapterUrl)
        refPdf.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes ->
                Log.d(TAG, "loadChapterPdf: $chapterUrl")

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        val currentPage = page + 1
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
}