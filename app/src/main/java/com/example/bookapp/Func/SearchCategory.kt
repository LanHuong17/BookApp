package com.example.bookapp.Func

import android.widget.Filter
import com.example.bookapp.Adapter.AdapterCategory
import com.example.bookapp.Model.ModelCategory

class SearchCategory: Filter {
    private var searchList: ArrayList<ModelCategory>
    private var adapterCategory: AdapterCategory

    constructor(searchList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.searchList = searchList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val result = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filterModel:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until searchList.size) {
                if (searchList[i].category.uppercase().contains(constraint)) {
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
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>
        adapterCategory.notifyDataSetChanged()
    }




//    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
//
//        val filteredList = results.values as? ArrayList<ModelCategory>
//        adapterCategory.categoryArrayList = filteredList ?: categoryArrayList
//        adapterCategory.notifyDataSetChanged()
//    }
}