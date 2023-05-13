package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.AdminActivity.BookListAdminActivity
import com.example.bookapp.Func.SearchCategory
import com.example.bookapp.Func.SearchCategoryUser
import com.example.bookapp.UserActivity.BookListActivity
import com.example.bookapp.databinding.ListCategoriesBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategoryUser :RecyclerView.Adapter<AdapterCategoryUser.ModelCategory>, Filterable{

    private val context: Context
    public var categoryArrayList: ArrayList<com.example.bookapp.Model.ModelCategory>
    private lateinit var binding: ListCategoriesBinding

    private var filter: SearchCategoryUser? = null
    private var searchList: ArrayList<com.example.bookapp.Model.ModelCategory>


    constructor(
        context: Context,
        categoryArrayList: ArrayList<com.example.bookapp.Model.ModelCategory>
    ) : super() {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.searchList = categoryArrayList
    }


    //viewholder để khởi tạo UI view cho list cate

    inner class ModelCategory(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvCategory: TextView = binding.tvCate
        var delete:ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelCategory {
        binding = ListCategoriesBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.deleteBtn.visibility=View.INVISIBLE
        return ModelCategory(binding.root)
    }

    override fun onBindViewHolder(holder: ModelCategory, position: Int) {
        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.tvCategory.text = category

        //handle click
        holder.delete.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete").setMessage("Are you sure?")
                .setPositiveButton("Confirm") {
                        a, d->
                    Toast.makeText(context, "Deleting", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)
                }
                .setNegativeButton("Cancel") {
                        a, d-> a.dismiss()
                }
                .show()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookListActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: com.example.bookapp.Model.ModelCategory, holder: ModelCategory) {
        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = SearchCategoryUser(searchList, this)
        }
        return filter as SearchCategoryUser
    }
}