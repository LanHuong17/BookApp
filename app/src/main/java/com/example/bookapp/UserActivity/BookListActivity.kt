package com.example.bookapp.UserActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterAllBook
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.databinding.ActivityBookListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookListBinding
    private lateinit var bookArrayList: ArrayList<ModelBook>
    private lateinit var adapterAllBook: AdapterAllBook

    private companion object {
        const val TAG = "PDF_LIST_USER_TAG"
    }

    private var categoryId = ""
    private var category = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!


        binding.tvCategory.text = category

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.tvSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterAllBook.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
        loadBookList()
//        displayBookList()
    }

    private fun loadBookList() {
        bookArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    bookArrayList.clear()
                    //get data
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelBook::class.java)
                        //add to list
                        if (model != null) {
                            bookArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }

                    }


                    val layoutManager = LinearLayoutManager(this@BookListActivity)
                    binding.listBooks.layoutManager = layoutManager

                    if (bookArrayList.isNotEmpty()) {
                        adapterAllBook = AdapterAllBook(this@BookListActivity, bookArrayList)
                        binding.listBooks.adapter = adapterAllBook
                    } else {
                        //...
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun displayBookList() {
        bookArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                bookArrayList.clear()
                //get data
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    bookArrayList.add(model!!)
                }

                val layoutManager = LinearLayoutManager(this@BookListActivity)
                binding.listBooks.layoutManager = layoutManager

                if (bookArrayList.isNotEmpty()) {
                    adapterAllBook = AdapterAllBook(this@BookListActivity, bookArrayList)
                    binding.listBooks.adapter = adapterAllBook
                } else {
                    //...
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}