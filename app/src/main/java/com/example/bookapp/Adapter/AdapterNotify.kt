package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.ReadBookActivity
import com.example.bookapp.databinding.ListNotifyBinding
import com.github.barteksc.pdfviewer.PDFView

class AdapterNotify : RecyclerView.Adapter<AdapterNotify.ModelNotify> {
    private val context: Context
    public var notifyArrayList: ArrayList<com.example.bookapp.Model.ModelNotify>
    private lateinit var binding: ListNotifyBinding

    constructor(context: Context, notifyArrayList: ArrayList<com.example.bookapp.Model.ModelNotify>) : super() {
        this.context = context
        this.notifyArrayList = notifyArrayList
    }

    inner class ModelNotify(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle = binding.titleTv
        var tvDate = binding.tvDate
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelNotify {
        binding = ListNotifyBinding.inflate(LayoutInflater.from(context), parent, false)
        return ModelNotify(binding.root)
    }

    override fun onBindViewHolder(holder: ModelNotify, position: Int) {
        val model = notifyArrayList[position]
        val id = model.id
        val uid = model.uid
        val bookId = model.bookId
        val titleBook = model.titleBook
        val titleChapter = model.titleChapter
        val url = model.url
        val timestamp = model.timestamp
        val dateFormat = MyApplication.formatTimestampToDateTime(timestamp)

        holder.tvTitle.text = "$titleBook has been added a new chapter: $titleChapter"
        holder.tvDate.text = dateFormat
        MyApplication.loadPdfFromUrlSinglePage(url, titleChapter, holder.pdfView, holder.progressBar, null)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ReadBookActivity::class.java)
            intent.putExtra("chapterId", id)
            context.startActivity(intent)
        }

//        MyApplication.loadBook(bookId = bookId, holder.tvTitle)
    }

    override fun getItemCount(): Int {
        return notifyArrayList.size
    }
}