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
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bookapp.Adapter.AdapterFavorite
import com.example.bookapp.Adapter.AdapterFragmentPage
import com.example.bookapp.EditProfile
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.LoginActivity
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentProfileBinding
import com.example.bookapp.databinding.ListFavoriteBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var favArrayList: ArrayList<ModelBook>
    private lateinit var adapterFavorite: AdapterFavorite
    private lateinit var adapterFragment: AdapterFragmentPage
    private lateinit var binding1: ListFavoriteBinding

    private companion object {
        const val TAG = "PROFILE_TAG"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(LayoutInflater.from(context), container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

        return binding.root
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.noAcc.visibility = View.VISIBLE
            binding.loginBtn.visibility = View.VISIBLE
            binding.layout.visibility = View.GONE
            binding.loginBtn.setOnClickListener {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        } else {
            binding.noAcc.visibility = View.GONE
            binding.loginBtn.visibility = View.GONE
            binding.layout.visibility = View.VISIBLE

            loadProfileInfo()
            val tabLayout = binding.tabLayout
            val viewPaper2 = binding.viewPager
            adapterFragment =
                AdapterFragmentPage(requireActivity().supportFragmentManager, lifecycle)

            tabLayout.addTab(tabLayout.newTab().setText("Favorites"))
            tabLayout.addTab(tabLayout.newTab().setText("Notification"))

            viewPaper2.adapter = adapterFragment

            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        viewPaper2.currentItem = tab.position
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }

            })

            viewPaper2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout.selectTab(tabLayout.getTabAt(position))
                }
            })

            binding.editProfile.setOnClickListener {
                startActivity(Intent(context, EditProfile::class.java))
            }

        }
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
                        Glide.with(this@ProfileFragment)
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

        favArrayList = ArrayList()

        val ref2 = FirebaseDatabase.getInstance().getReference("Users")
        ref2.child(firebaseAuth.uid!!).child("Favorites")
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
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}
