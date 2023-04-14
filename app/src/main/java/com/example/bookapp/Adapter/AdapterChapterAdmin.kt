package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.BookDetailActivity
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Model.ModelChapter
import com.example.bookapp.ReadBookActivity
import com.example.bookapp.databinding.ActivityBookDetailBinding
import com.example.bookapp.databinding.ListBooksBinding
import com.example.bookapp.databinding.ListCategoriesBinding
import com.example.bookapp.databinding.ListChaptersBinding

class AdapterChapterAdmin : RecyclerView.Adapter<AdapterChapterAdmin.ModelChapter> {
    private val context: Context
    public var chapterArrayList: ArrayList<com.example.bookapp.Model.ModelChapter>
    private lateinit var binding: ListChaptersBinding

    constructor(context: Context, chapterArrayList: ArrayList<com.example.bookapp.Model.ModelChapter>) : super() {
        this.context = context
        this.chapterArrayList = chapterArrayList
    }

    inner class ModelChapter(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle = binding.tvTitle
        var tvCountView = binding.tvCountView
        var tvDate = binding.tvDate
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelChapter {
        binding = ListChaptersBinding.inflate(LayoutInflater.from(context), parent, false)
        return ModelChapter(binding.root)
    }

    override fun onBindViewHolder(holder: ModelChapter, position: Int) {
        val model = chapterArrayList[position]
        val id = model.id
        val uid = model.uid
        val bookId = model.bookId
        val titleBook = model.titleBook
        val titleChapter = model.titleChapter
        val url = model.url
        val viewCount = model.viewCount
        val downloadCount = model.downloadCount
        val timestamp = model.timestamp
        val dateFormat = MyApplication.formatTimeStamp(timestamp)

        holder.tvTitle.text = titleChapter
        holder.tvCountView.text = viewCount.toString()
        holder.tvDate.text = dateFormat

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ReadBookActivity::class.java)
            intent.putExtra("chapterId", id)
            context.startActivity(intent)
        }

//        MyApplication.loadBook(bookId = bookId, holder.tvTitle)
    }

    override fun getItemCount(): Int {
        return chapterArrayList.size
    }
}