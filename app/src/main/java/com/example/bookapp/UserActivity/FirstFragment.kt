package com.example.bookapp.UserActivity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterFavorite
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentFavoriteBinding
import com.example.bookapp.databinding.FragmentFirstBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirstFragment : Fragment() {
    private lateinit var binding: FragmentFirstBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favArrayList: ArrayList<ModelBook>
    private lateinit var adapterFavorite: AdapterFavorite

    private companion object {
        const val TAG = "PROFILE_TAG"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = FragmentFirstBinding.inflate(LayoutInflater.from(context), container, false)

        firebaseAuth = FirebaseAuth.getInstance()

//        loadProfileInfo()
        loadFavoriteBook()

        return binding.root
    }

    private fun loadFavoriteBook() {
        favArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favArrayList.clear()
                    for (ds in snapshot.children) {
                        val bookId = "${ds.child("bookId").value}"

                        val modelBook = ModelBook()
                        modelBook.id = bookId

                        favArrayList.add(modelBook)
                    }
                    binding.tvSubFav.text = "${favArrayList.size}"

                    val layoutManager = LinearLayoutManager(context)
                    binding.listFav.layoutManager = layoutManager

                    if (favArrayList.isNotEmpty()) {
                        if (context == null) {
                            Log.d(TAG, "Check Null: context is null")
                        } else {
                            adapterFavorite = AdapterFavorite(context!!, favArrayList)
                            binding.listFav.adapter = adapterFavorite
                        }
                    } else {
                        //...
                    }

                    binding.listFav.layoutManager= GridLayoutManager(
                        context,
                        3,
                        LinearLayoutManager.VERTICAL,
                        false
                    )
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}