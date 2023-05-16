package com.example.bookapp.UserActivity

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import java.util.*
import kotlin.collections.ArrayList

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategoryUser: AdapterCategoryUser
    private val REQUEST_CODE_SPEECH_INPUT = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(LayoutInflater.from(context), container, false)

        displayCategory()
        binding.voiceBtn.setOnClickListener{
            askSpeechInput()
        }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {

                val res: ArrayList<String> =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                binding.tvSearch.setText(
                    Objects.requireNonNull(res)[0]
                )
            }
        }
    }

    private fun askSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast
                .makeText(context, " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
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
                    if(context == null) {
                        Log.d("CHECK NULL", "Check null: Context is null")
                    } else {
                        adapterCategoryUser = AdapterCategoryUser(context!!, categoryArrayList)
                        binding.listCategories.adapter = adapterCategoryUser
                    }

                } else {

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}