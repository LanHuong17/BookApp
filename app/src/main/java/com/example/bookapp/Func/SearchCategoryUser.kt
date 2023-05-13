package com.example.bookapp.Func

import android.widget.Filter
import com.example.bookapp.Adapter.AdapterCategory
import com.example.bookapp.Adapter.AdapterCategoryUser
import com.example.bookapp.Model.ModelCategory

class SearchCategoryUser: Filter {
    private var searchList: ArrayList<ModelCategory>
    private var adapterCategoryUser: AdapterCategoryUser

    constructor(searchList: ArrayList<ModelCategory>, adapterCategoryUser: AdapterCategoryUser) : super() {
        this.searchList = searchList
        this.adapterCategoryUser = adapterCategoryUser
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
        adapterCategoryUser.categoryArrayList = results.values as ArrayList<ModelCategory>
        adapterCategoryUser.notifyDataSetChanged()
    }




//    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
//
//        val filteredList = results.values as? ArrayList<ModelCategory>
//        adapterCategory.categoryArrayList = filteredList ?: categoryArrayList
//        adapterCategory.notifyDataSetChanged()
//    }
}