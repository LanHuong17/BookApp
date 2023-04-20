package com.example.bookapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.Func.MyApplication
import com.example.bookapp.Model.ModelComment
import com.example.bookapp.R
import com.example.bookapp.databinding.ListCommentsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterComment: RecyclerView.Adapter<AdapterComment.ModelComment> {
    val context: Context
    val commentArrayList: ArrayList<com.example.bookapp.Model.ModelComment>
    private lateinit var binding: ListCommentsBinding
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(
        context: Context,
        commentArrayList: ArrayList<com.example.bookapp.Model.ModelComment>
    ) {
        this.context = context
        this.commentArrayList = commentArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class ModelComment(itemView: View): RecyclerView.ViewHolder(itemView) {
        val userAvatar = binding.userAvatar
        val usernameTv = binding.usernameTv
        val commentTv = binding.commentTv
        val commentDate = binding.commentDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelComment {
        binding = ListCommentsBinding.inflate(LayoutInflater.from(context), parent, false)
        return ModelComment(binding.root)
    }

    override fun onBindViewHolder(holder: ModelComment, position: Int) {
        val model = commentArrayList[position]

        val id = model.id
        val uid = model.uid
        val bookId = model.bookId
        val timestamp = model.timestamp
        val comment = model.comment

        val date = MyApplication.formatTimeStamp(timestamp)

        holder.commentDate.text = date
        holder.commentTv.text = comment

        loadUserDetail(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: com.example.bookapp.Model.ModelComment, holder: AdapterComment.ModelComment) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete comment")
            .setMessage("Are you sure delete this comment?")
            .setPositiveButton("Delete") {d, e ->
                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e->
                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel") {d, e->
                d.dismiss()
            }.show()
    }

    private fun loadUserDetail(model: com.example.bookapp.Model.ModelComment, holder: AdapterComment.ModelComment) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    holder.usernameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.person_gray)
                            .into(holder.userAvatar)
                    } catch (e: Exception) {
                        holder.userAvatar.setImageResource(R.drawable.person_gray)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //...
                }

            })
    }

    override fun getItemCount(): Int {
       return commentArrayList.size
    }
}