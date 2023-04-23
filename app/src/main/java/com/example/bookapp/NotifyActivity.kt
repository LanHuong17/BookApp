package com.example.bookapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterBookAdmin
import com.example.bookapp.Adapter.AdapterNotify
import com.example.bookapp.AdminActivity.BookListAdminActivity
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.Model.ModelNotify
import com.example.bookapp.databinding.ActivityNotifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotifyBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var notifyArrayList: ArrayList<ModelNotify>
    private lateinit var adapterNotify: AdapterNotify

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNotifyBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        loadNotifyList()
    }

    private fun loadNotifyList() {
        notifyArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Notifies")
        ref.addValueEventListener(object: ValueEventListener{

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

                        val layoutManager = LinearLayoutManager(this@NotifyActivity)
                        binding.notifyLists.layoutManager = layoutManager

                        if (filteredList.isNotEmpty()) {
                            adapterNotify = AdapterNotify(this@NotifyActivity,
                                filteredList as ArrayList<ModelNotify>
                            )
                            binding.notifyLists.adapter = adapterNotify
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