package com.example.bookapp.UserActivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.inflate
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterFavorite
import com.example.bookapp.Adapter.AdapterNotify
import com.example.bookapp.Model.ModelNotify
import com.example.bookapp.databinding.ActivityAddBookBinding.inflate
import com.example.bookapp.databinding.ActivityNotifyBinding
import com.example.bookapp.databinding.FragmentFirstBinding
import com.example.bookapp.databinding.FragmentNotifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotifyFragment : Fragment() {

    private lateinit var binding: FragmentNotifyBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var notifyArrayList: ArrayList<ModelNotify>
    private lateinit var adapterNotify: AdapterNotify


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = FragmentNotifyBinding.inflate(LayoutInflater.from(context), container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        loadNotifyList()
        return binding.root
    }

    private fun loadNotifyList() {
        notifyArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Notifies")
        ref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                notifyArrayList.clear()
                //get data
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelNotify::class.java)
                    //add to list
                    if (model != null) {
                        notifyArrayList.add(model)
                    }
                }
                notifyArrayList.sortByDescending { it.timestamp }

                // get favorite books
                val favoriteRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(firebaseAuth.uid!!)
                    .child("Favorites")

                favoriteRef.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bookIds = ArrayList<String>()
                        for (ds in snapshot.children) {
                            bookIds.add(ds.key!!)
                        }

                        // filter notifyArrayList by bookIds
                        val filteredList = notifyArrayList.filter { bookIds.contains(it.bookId) }

                        val layoutManager = LinearLayoutManager(context)
                        binding.notifyLists.layoutManager = layoutManager

                        if (filteredList.isNotEmpty()) {
                            if (context == null) {
                                Log.d("CHECK NULL", "Check Null: context is null")
                            } else {
                                adapterNotify = AdapterNotify(context!!,
                                    filteredList as ArrayList<ModelNotify>
                                )
                                binding.notifyLists.adapter = adapterNotify
                            }
                        } else {
                            //...
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}