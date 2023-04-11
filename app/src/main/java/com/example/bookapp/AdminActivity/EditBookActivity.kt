package com.example.bookapp.AdminActivity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.Func.MyApplication.Companion.loadCategory
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityEditBookBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditBookActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    private lateinit var binding: ActivityEditBookBinding
    //from intent started from AdapterBook
    private var bookId = ""
    private lateinit var progressDialog: ProgressDialog
    private lateinit var categoryTitleArrayList:ArrayList<String>
    private lateinit var categoryIdArrayList:ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEditBookBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.pickCate.setOnClickListener {
            categoryDialog()
        }

        binding.editBook.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book's info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    binding.inputTitle.setText(title)
                    binding.inputDescript.setText(description)

                    //get book's category
                    Log.d(TAG, "loadBookInfo: Loading category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val category = snapshot.child("category").value
                                binding.pickCate.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var title= ""
    private var description = ""

    private fun validateData() {
        title = binding.inputTitle.text.toString().trim()
        description = binding.inputDescript.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Pls enter title", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Pls enter description", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Pls pick category", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting update")

        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully")
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { f->
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated failed ${f.message}")
                Toast.makeText(this, "Updated failed: ${f.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategory = ""

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { dialog, position->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategory = categoryTitleArrayList[position]

                binding.pickCate.text = selectedCategory
            }.show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories")
        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: CategoryID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}