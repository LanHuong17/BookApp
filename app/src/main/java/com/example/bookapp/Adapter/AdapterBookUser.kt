package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView
import android.view.View
import android.widget.*
import com.example.bookapp.BookDetailActivity
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Func.MyApplication.Companion.formatTimeStamp
import com.example.bookapp.databinding.ListBooksItemBinding
import kotlin.collections.ArrayList

class AdapterBookUser: RecyclerView.Adapter<AdapterBookUser.ModelBook> {
    private lateinit var context: Context
    private lateinit var bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>
    private lateinit var binding: ListBooksItemBinding

    private companion object {
        const val TAG = "ADAPTER_BOOK_USER"
    }

    constructor(
        context: Context,
        bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>,

        ) : super() {
        this.context = context
        this.bookArrayList = bookArrayList
    }

    inner class ModelBook(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = binding.titleTv
        var tvCategory: TextView = binding.tvCategory
//        var tvDescription: TextView = binding.tvDescription
        var tvTotalView: TextView = binding.totalView
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var tvTotalDownload: TextView = binding.totalDownload
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelBook {
        binding = ListBooksItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ModelBook(binding.root)
    }

    override fun onBindViewHolder(holder: ModelBook, position: Int) {
        val model = bookArrayList[position]
        val id = model.id
        val uid = model.uid
        val title = model.title
        val description = model.description
        val categoryId = model.categoryId
        val url = model.url
        val timestamp = model.timestamp
        val viewTotal = model.viewCount
        val downloadTotal = model.downloadCount

//        val date = formatTimeStamp(timestamp)
//        if (date != null) {
//            holder.tvDate.text = formatTimeStamp(timestamp)
//        } else {
//            holder.tvDate.text = "Invalid timestamp"
//        }

        holder.tvTitle.text = title
//        holder.tvDescription.text = description

        holder.tvTotalView.text = viewTotal.toString()
        holder.tvTotalDownload.text = downloadTotal.toString()


        MyApplication.loadCategory(categoryId = categoryId, holder.tvCategory)
        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)


        holder.itemView.setOnClickListener {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra("bookId", id)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return bookArrayList.size
    }



}