package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.bookapp.Func.Constants
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.databinding.ActivityReadBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ReadBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadBookBinding
    var chapterId = ""

    private companion object {
        const val TAG = "CHAPTER_DETAIL_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityReadBookBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        chapterId = intent.getStringExtra("chapterId")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        loadChapterDetail()
    }

    private fun loadChapterDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.child(chapterId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val url = "${snapshot.child("url").value}"
                    val title = "${snapshot.child("titleChapter").value}"

                    binding.tvTitleChapter.text = title

                    Log.d(TAG, "onDataChange: PDF_URL: $url")
                    Log.d(TAG, "onDataChange: TitleChapter: $title")

                    loadChapterPdf("$url")
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