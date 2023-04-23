package com.example.bookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.bookapp.Adapter.AdapterCategory
import com.example.bookapp.Adapter.AdapterFavorite
import com.example.bookapp.AdminActivity.AddCategoryActivity
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favArrayList: ArrayList<ModelBook>
    private lateinit var adapterFavorite: AdapterFavorite

    private companion object {
        const val TAG = "PROFILE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityProfileBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfile::class.java))
        }

        binding.notifyBtn.setOnClickListener {
            startActivity(Intent(this, NotifyActivity::class.java))
        }

        loadProfileInfo()
        loadFavoriteBook()
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

                    val layoutManager = LinearLayoutManager(this@ProfileActivity)
                    binding.listFav.layoutManager = layoutManager

                    if (favArrayList.isNotEmpty()) {
                        adapterFavorite = AdapterFavorite(this@ProfileActivity, favArrayList)
                        binding.listFav.adapter = adapterFavorite
                    } else {

                    }
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
                        Glide.with(this@ProfileActivity)
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