package com.example.bookapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.databinding.ListBooksBinding
import com.github.barteksc.pdfviewer.PDFView
import android.view.View
import android.widget.*
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Func.SearchBook

class AdapterBookAdmin :RecyclerView.Adapter<AdapterBookAdmin.ModelBook>, Filterable{
    private val context: Context
    public var bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>
    private lateinit var binding: ListBooksBinding
    private val filterList: ArrayList<com.example.bookapp.Model.ModelBook>

    private var filter : SearchBook? = null

    constructor(
        context: Context,
        bookArrayList: ArrayList<com.example.bookapp.Model.ModelBook>,

    ) : super() {
        this.context = context
        this.bookArrayList = bookArrayList
        this.filterList = bookArrayList
    }

    inner class ModelBook(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvTitle: TextView = binding.titleTv
        var tvDescription: TextView = binding.tvDescription
        var tvCategory: TextView = binding.tvCategory
        var tvDate: TextView = binding.tvDate
        var pdfView: PDFView = binding.pdfView
        var progressBar: ProgressBar = binding.progressBar
        var moreBtn: ImageButton = binding.moreBtn
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelBook {
        binding = ListBooksBinding.inflate(LayoutInflater.from(context), parent, false)
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
//        val dateFormat = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = "dd/MM/yyyy"

        MyApplication.loadCategory(categoryId = categoryId, holder.tvCategory)
        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)
    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null) {
            filter = SearchBook(filterList, this)
        }
        return filter as SearchBook
    }

}