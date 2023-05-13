package com.example.bookapp.UserActivity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterCategory
import com.example.bookapp.Adapter.AdapterCategoryUser
import com.example.bookapp.Model.ModelCategory
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentCategoryBinding
import com.example.bookapp.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategoryUser: AdapterCategoryUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(LayoutInflater.from(context), container, false)

        displayCategory()

        binding.tvSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategoryUser.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return binding.root
    }

    private fun displayCategory() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()
                //get data
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                }

                val layoutManager = LinearLayoutManager(context)
                binding.listCategories.layoutManager = layoutManager

                if (categoryArrayList.isNotEmpty()) {
                    adapterCategoryUser = AdapterCategoryUser(context!!, categoryArrayList)
                    binding.listCategories.adapter = adapterCategoryUser
                } else {

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}