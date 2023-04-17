package com.example.bookapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterChapterAdmin
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Model.ModelChapter
import com.example.bookapp.databinding.ActivityBookDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookDetailActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBookDetailBinding
    private lateinit var chapterArrayList: ArrayList<ModelChapter>
    private lateinit var adapterChapter: AdapterChapterAdmin
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        const val TAG = "BOOK_DETAIL_TAG"
    }

    private var bookId = ""
    private var chapterId = ""
    private var isFavorite = false


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBookDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        bookId = intent.getStringExtra("bookId")!!

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addFavorite.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logging!", Toast.LENGTH_SHORT).show()
            } else {
                if (isFavorite) {
                    removeFromFavorite()
                } else {
                    addToFavorite()
                }
            }
        }

        checkFavorite()
        loadBookDetail()
        loadBookChapter()
    }

    private fun checkFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    if (isFavorite) {
                        Log.d(TAG, "Exists")
                        val drawable = ContextCompat.getDrawable(this@BookDetailActivity, R.drawable.full_heart)
                        binding.addFavorite.setImageDrawable(drawable)
                    } else {
                        val drawable = ContextCompat.getDrawable(this@BookDetailActivity, R.drawable.heart)
                        binding.addFavorite.setImageDrawable(drawable)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addToFavorite() {
        val timestamp = System.currentTimeMillis()
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .setValue(hashMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Added to favorite")
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "failed add to favorite: ${e.message}")
                    Toast.makeText(this, "failed add to favorite: ${e.message}", Toast.LENGTH_SHORT).show()
                }
    }

    private fun removeFromFavorite() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "Removed from favorite")
            }
            .addOnFailureListener { e->
                Log.d(TAG, "failed remove from favorite: ${e.message}")
                Toast.makeText(this, "failed remove from favorite: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBookChapter() {
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


                    val layoutManager = LinearLayoutManager(this@BookDetailActivity)
                    binding.listChapters.layoutManager = layoutManager

                    if (chapterArrayList.isNotEmpty()) {
                        adapterChapter = AdapterChapterAdmin(this@BookDetailActivity, chapterArrayList)
                        binding.listChapters.adapter = adapterChapter
                    } else {
                        //...
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookDetail() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val totalViewBook = "${snapshot.child("viewCount").value}"


                    val date = MyApplication.formatTimeStamp(timestamp)

                    MyApplication.loadCategory(categoryId, binding.tvCategory)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", binding.pdfView, binding.progressBar, null)


                    binding.tvTitleBook.text = title
                    binding.tvDescription.text = description
                    binding.tvDate.text = date

                    calculateTotalViewCount(bookId)
                    calculateTotalDownloadCount(bookId)

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun calculateTotalViewCount(bookId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.orderByChild("bookId").equalTo(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalViewCount = 0L
                    for (chapterSnapshot in snapshot.children) {
                        val viewCount = chapterSnapshot.child("viewCount").value.toString().toLong()
                        totalViewCount += viewCount
                    }
                    binding.tvViewTotal.text = "$totalViewCount"
                }

                override fun onCancelled(error: DatabaseError) {
                    //...
                }
            })
    }

    fun calculateTotalDownloadCount(bookId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Chapters")
        ref.orderByChild("bookId").equalTo(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalDownloadCount = 0L
                    for (chapterSnapshot in snapshot.children) {
                        val downloadCount = chapterSnapshot.child("downloadCount").value.toString().toLong()
                        totalDownloadCount += downloadCount
                    }
                    binding.tvTotalDownload.text = "$totalDownloadCount"
                }

                override fun onCancelled(error: DatabaseError) {
                    //...
                }
            })
    }
}