package com.example.bookapp.AdminActivity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.Adapter.AdapterCategory
import com.example.bookapp.MainActivity
import com.example.bookapp.Model.ModelCategory
import com.example.bookapp.ProfileActivity
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory:AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()
        displayCategory()

        //handle logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //handle add cate
        binding.addCate.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
        }

        binding.addPdf.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.tvSearch.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun displayCategory() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list
                categoryArrayList.clear()
                //get data
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                }

                val layoutManager = LinearLayoutManager(this@DashboardAdminActivity)
                binding.listCategories.layoutManager = layoutManager

                if (categoryArrayList.isNotEmpty()) {
                    adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                    binding.listCategories.adapter = adapterCategory
                } else {

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            val email = firebaseUser.email
            binding.tvSubTitle.text = email
        }
    }
}