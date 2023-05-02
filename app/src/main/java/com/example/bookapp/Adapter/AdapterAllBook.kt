package com.example.bookapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.BookDetailActivity
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Func.SearchBook
import com.example.bookapp.Func.SearchBookUser
import com.example.bookapp.databinding.ListBooksBinding
import com.github.barteksc.pdfviewer.PDFView

class AdapterAllBook : RecyclerView.Adapter<AdapterAllBook.ModelBook>, Filterable{
    private lateinit var context: Context
    lateinit var bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>
    private lateinit var binding: ListBooksBinding
    public val filterList: ArrayList<com.example.bookapp.Model.ModelBook>

    private var filter : SearchBookUser? = null

    constructor(
        context: Context,
        bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>,

        ) : super() {
        this.context = context
        this.bookArrayList = bookArrayList
        this.filterList = bookArrayList
    }


    inner class ModelBook(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = binding.titleTv
        var tvCategory: TextView = binding.tvCategory
        var tvDescription: TextView = binding.tvDescription
        var tvDate: TextView = binding.tvDate
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var moreBtn: ImageButton = binding.moreBtn
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelBook {
        binding = ListBooksBinding.inflate(LayoutInflater.from(context), parent, false)
        binding.moreBtn.visibility= View.GONE
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
            intent.putExtra("bookId", id)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = SearchBookUser(filterList, this)
        }
        return filter as SearchBookUser
    }
}