package com.example.bookapp.UserActivity

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.denzcoskun.imageslider.models.SlideModel
import com.example.bookapp.Adapter.*
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.R
import com.example.bookapp.databinding.FragmentHomeBinding
import com.example.bookapp.databinding.ListBooksItemBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.util.Pair
import com.blogspot.atifsoftwares.animatoolib.Animatoo


class HomeFragment : Fragment {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var subBinding: ListBooksItemBinding

    private companion object {
        const val TAG = "BOOKS_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): HomeFragment{
            val fragment = HomeFragment()
            //put data to  bundle  intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""


    lateinit var viewArrayList: ArrayList<ModelBook>
    lateinit var downloadArrayList: ArrayList<ModelBook>
    lateinit var pdfArrayList: ArrayList<ModelBook>
    private lateinit var adapterBookUser: AdapterBookUser
    private lateinit var adapterAllBook: AdapterAllBook
    private lateinit var sliderView: ViewPager2
    private val REQUEST_CODE_SPEECH_INPUT = 1


    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments that passed in newInstance method
        val args = arguments
        if(args !=null){
            categoryId=args.getString("categoryId")!!
            category=args.getString("category")!!
            uid=args.getString("uid")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(context), container, false)
        subBinding = ListBooksItemBinding.inflate(LayoutInflater.from(context), container, false)

        loadSliders()
        loadAllBooks()
        loadMostViewedBooks()
        loadMostDownloadedBooks()
        checkToken()

        binding.voiceBtn.setOnClickListener{
            askSpeechInput()
        }

        binding.tvSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    binding.imageSlider.visibility= View.GONE
                    binding.textView1.visibility= View.GONE
                    binding.textView2.visibility= View.GONE
                    binding.textView3.visibility= View.GONE
                    binding.rcv1.visibility= View.GONE
                    binding.rcv2.visibility= View.GONE

                    if(binding.tvSearch.text.isEmpty()){
                        binding.imageSlider.visibility= View.VISIBLE
                        binding.textView1.visibility= View.VISIBLE
                        binding.textView2.visibility= View.VISIBLE
                        binding.textView3.visibility= View.VISIBLE
                        binding.rcv1.visibility= View.VISIBLE
                        binding.rcv2.visibility= View.VISIBLE
                    }

                    adapterAllBook.filter.filter(s)
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

    private fun loadSliders() {
        val imageList = ArrayList<SlideModel>() // Create image list

// imageList.add(SlideModel("String Url" or R.drawable)
// imageList.add(SlideModel("String Url" or R.drawable, "title") You can add title

        imageList.add(SlideModel(R.drawable.bs))
        imageList.add(SlideModel(R.drawable.bs1))
        imageList.add(SlideModel(R.drawable.bs2))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList)
    }

    private fun checkToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "Token : $token")

        })
    }

    private fun loadMostDownloadedBooks() {
        subBinding.totalView.visibility = View.INVISIBLE
        downloadArrayList = ArrayList()
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("downloadCount").limitToLast(4)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    downloadArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelBook::class.java)
                        //add to list
                        if (model != null) {
                            downloadArrayList.add(model)
                        }
                    }
                    //setup adapter
                    val layoutManager = LinearLayoutManager(context)
                    binding.rcv2.layoutManager = layoutManager

                    if (downloadArrayList.isNotEmpty()) {
                        if(context == null) {
                            Log.d(TAG, "Check null: Context is null")
                        } else {
                            adapterBookUser = AdapterBookUser(context!!, downloadArrayList)
                            binding.rcv2.adapter = adapterBookUser
                        }

                    } else {
                        //...
                    }
                    downloadArrayList.reverse()
                    binding.rcv2.layoutManager= LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }


                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadMostViewedBooks() {
        subBinding.totalDownload.visibility = View.GONE
        viewArrayList = ArrayList()

        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("viewCount").limitToLast(5)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelBook::class.java)
                        //add to list
                        if (model != null) {
                            viewArrayList.add(model)
                        }

                    }
                    //setup adapter
                    val layoutManager = LinearLayoutManager(context)
                    binding.rcv1.layoutManager = layoutManager


                    if (viewArrayList.isNotEmpty()) {
                        if(context == null) {
                            Log.d(TAG, "Check null: Context is null")
                        } else {
                            adapterBookUser = AdapterBookUser(context = context!!, viewArrayList)
                            binding.rcv1.adapter = adapterBookUser
                        }

                    } else {
                        //...
                    }
                    viewArrayList.reverse()
                    binding.rcv1.layoutManager= LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.rcv1.invalidate()
                }


                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.limitToLast(5).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    if (model != null) {
                        pdfArrayList.add(model)
                    }
                }
                //setup adapter
                val layoutManager = LinearLayoutManager(context)
                binding.rcv3.layoutManager = layoutManager

                if (pdfArrayList.isNotEmpty()) {
                    if(context == null) {
                        Log.d(TAG, "Check null: Context is null")
                    } else {
                        adapterAllBook = AdapterAllBook(context!!, pdfArrayList)
                        binding.rcv3.adapter = adapterAllBook
                    }

                } else {
                    //...
                }

//                adapterAllBook = AdapterAllBook(context!!, pdfArrayList)
//                binding.rcv3.adapter = adapterAllBook
            }


            override fun onCancelled(error: DatabaseError) {
            }
        })
    }



}