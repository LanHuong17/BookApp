package com.example.bookapp.UserActivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bookapp.Adapter.AdapterFavorite
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.LoginActivity
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.R
import com.example.bookapp.RegisterActivity
import com.example.bookapp.databinding.FragmentFavoriteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favArrayList: ArrayList<ModelBook>
    private lateinit var adapterFavorite: AdapterFavorite

    private companion object {
        const val TAG = "PROFILE_TAG"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        binding = FragmentFavoriteBinding.inflate(LayoutInflater.from(context), container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

//        loadProfileInfo()


        return binding.root
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.noAcc.visibility = View.VISIBLE
            binding.loginBtn.visibility = View.VISIBLE
            binding.listFav.visibility = View.GONE
            binding.loginBtn.setOnClickListener{
                startActivity(Intent(context, LoginActivity::class.java))
            }
        } else {
            binding.noAcc.visibility = View.GONE
            binding.loginBtn.visibility = View.GONE
            binding.listFav.visibility = View.VISIBLE
            loadFavoriteBook()
        }
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

    private fun loadProfileInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"

                    val date = MyApplication.formatTimeStamp(timestamp)
                    binding.tvSubAcc.text = userType
                    binding.tvSubMem.text = date
//                    binding.tvSubFav.text = ""

                    binding.tvName.text = name
                    binding.tvEmail.text = email

                    //set image

                    try {
                        Glide.with(this@FavoriteFragment)
                            .load(profileImage)
                            .placeholder(R.drawable.person_gray)
                            .override(120, 120)
                            .into(binding.avatar)
                    } catch (e: Exception) {
                        //...
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //...
                }

            })
    }
}