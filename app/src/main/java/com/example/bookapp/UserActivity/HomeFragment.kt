package com.example.bookapp.UserActivity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.bookapp.Adapter.AdapterAllBook
import com.example.bookapp.Adapter.AdapterBookAdmin
import com.example.bookapp.Adapter.AdapterBookUser
import com.example.bookapp.Model.ModelBook
import com.example.bookapp.databinding.FragmentHomeBinding
import com.example.bookapp.databinding.ListBooksItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var subBinding: ListBooksItemBinding
    private var isMostViewed: Boolean = true

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

        loadAllBooks()
        loadMostViewedBooks()
        loadMostDownloadedBooks()


        binding.tvSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    binding.sliderView.visibility= View.GONE
                    binding.textView1.visibility= View.GONE
                    binding.textView2.visibility= View.GONE
                    binding.textView3.visibility= View.GONE
                    binding.rcv1.visibility= View.GONE
                    binding.rcv2.visibility= View.GONE

                    if(binding.tvSearch.text.isEmpty()){
                        binding.sliderView.visibility= View.VISIBLE
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

    private fun loadMostDownloadedBooks() {
        subBinding.totalView.visibility = View.INVISIBLE
        downloadArrayList = ArrayList()
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("downloadCount").limitToLast(5)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    downloadArrayList.clear()
                    for(ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelBook::class.java)
                        //add to list
                        downloadArrayList.add(model!!)
                    }
                    //setup adapter
                    val layoutManager = LinearLayoutManager(context)
                    binding.rcv2.layoutManager = layoutManager

                    if (downloadArrayList.isNotEmpty()) {
                        adapterBookUser = AdapterBookUser(context!!, downloadArrayList)
                        binding.rcv2.adapter = adapterBookUser
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
                        viewArrayList.add(model!!)
                    }
                    //setup adapter
                    val layoutManager = LinearLayoutManager(context)
                    binding.rcv1.layoutManager = layoutManager

                    if (viewArrayList.isNotEmpty()) {
                        adapterBookUser = AdapterBookUser(context!!, viewArrayList)
                        binding.rcv1.adapter = adapterBookUser
                    } else {
                        //...
                    }
                    viewArrayList.reverse()
                    binding.rcv1.layoutManager= LinearLayoutManager(
                        context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                }


                override fun onCancelled(error: DatabaseError) {
                }
            })

    }

    private fun loadAllBooks() {
        pdfArrayList = ArrayList()
        var ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.limitToFirst(10).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for(ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelBook::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                val layoutManager = LinearLayoutManager(context)
                binding.rcv3.layoutManager = layoutManager

                if (pdfArrayList.isNotEmpty()) {
                    adapterAllBook = AdapterAllBook(context!!, pdfArrayList)
                    binding.rcv3.adapter = adapterAllBook
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