package com.example.bookapp.Func

import android.widget.Filter
import com.example.bookapp.Adapter.AdapterAllBook
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.Model.ModelCategory
import com.example.bookapp.databinding.FragmentHomeBinding

class SearchBookUser: Filter {
    private var searchList: ArrayList<ModelBook>
    private var adapterAllBook: AdapterAllBook

    constructor(searchList: ArrayList<ModelBook>, adapterAllBook: AdapterAllBook) : super() {
        this.searchList = searchList
        this.adapterAllBook = adapterAllBook
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val result = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel:ArrayList<ModelBook> = ArrayList()
            for (i in 0 until searchList.size) {
                if (searchList[i].title.uppercase().contains(constraint)) {
                    filterModel.add(searchList[i])
                }
            }
            result.count = filterModel.size
            result.values = filterModel
        } else {
            result.count = searchList.size
            result.values = searchList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterAllBook.bookArrayList = results.values as ArrayList<ModelBook>
        adapterAllBook.notifyDataSetChanged()
    }
}