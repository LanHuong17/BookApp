package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.BookDetailActivity
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.Model.ModelFavorite
import com.example.bookapp.databinding.ListFavoriteBinding
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterFavorite : RecyclerView.Adapter<AdapterFavorite.ModelFavorite> {
    private val context: Context
    public var favArrayList: ArrayList<ModelBook>

    private lateinit var binding: ListFavoriteBinding

    constructor(context: Context, favArrayList: ArrayList<ModelBook>) : super() {
        this.context = context
        this.favArrayList = favArrayList
    }


    inner class ModelFavorite(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = binding.titleTv
        var tvDescription: TextView = binding.tvDescription
        var tvCategory: TextView = binding.tvCategory
        var tvDate: TextView = binding.tvDate
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelFavorite {
        binding = ListFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return ModelFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: ModelFavorite, position: Int) {
        val model = favArrayList[position]

        loadBookDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        binding.removeFavorite.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()

                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(firebaseAuth.uid!!).child("Favorites").child(model.id)
                    .removeValue()
                    .addOnSuccessListener {
                        //...
                    }
                    .addOnFailureListener { e->
                        //...
                    }
        }
    }

    private fun loadBookDetails(model: ModelBook, holder: ModelFavorite) {

        val bookId =  model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val uid = "${snapshot.child("uid").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val url = "${snapshot.child("url").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"

                    //set data to model

                    model.isFavorite = true
                    model.uid = uid
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp
                    model.url = url

                    val date = MyApplication.formatTimeStamp(timestamp)
                    if (date != null) {
                        holder.tvDate.text = MyApplication.formatTimeStamp(timestamp)
                    } else {
                            holder.tvDate.text = "Invalid timestamp"
                    }

                    holder.tvTitle.text = title
                    holder.tvDescription.text = description

                    MyApplication.loadCategory(categoryId = categoryId, holder.tvCategory)
                    MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

                    holder.itemView.setOnClickListener {
                        val intent = Intent(context, BookDetailActivity::class.java)
                        intent.putExtra("bookId", model.id)
                        context.startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //...
                }

            })

    }


    override fun getItemCount(): Int {
        return favArrayList.size
    }
}