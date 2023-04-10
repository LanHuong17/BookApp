package com.example.bookapp.Func

import android.widget.Filter
import com.example.bookapp.Adapter.AdapterBookAdmin
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.Model.ModelCategory

class SearchBook: Filter {
    private var searchList: ArrayList<ModelBook>
    private var adapterBook: AdapterBookAdmin

    constructor(searchList: ArrayList<ModelBook>, adapterBook: AdapterBookAdmin) : super() {
        this.searchList = searchList
        this.adapterBook = adapterBook
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
        adapterBook.bookArrayList = results.values as ArrayList<ModelBook>
        adapterBook.notifyDataSetChanged()
    }
}