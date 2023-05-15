package com.example.bookapp.Func

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookapp.AdminActivity.AddChapterActivity
import com.example.bookapp.NotifyActivity
import com.example.bookapp.R
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.collections.HashMap


class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }


    private lateinit var firebaseAuth: FirebaseAuth

    companion object {

        const val TAG = "FCM_SEND"

        fun formatTimeStamp(timestamp: String): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp.toLong()
            return android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun formatTimestampToDateTime(timestamp: String): String {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("HH:mm, dd/MM/yyyy", Locale.getDefault())
            return format.format(date)
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {
            val TAG = "PDF_TAG"

            //using url get file and its metatdata form firebase
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfFromUrlSinglePage: PDF_URL: $pdfUrl")
                    Log.d(TAG, "")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            progressBar.visibility = View.INVISIBLE
                            if (pagesTv != null) {
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "loadPdfFromUrlSinglePage: ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            //load category using id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = "${snapshot.child("category").value}"
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun loadBook(bookId: String, bookTv: TextView) {
            //load category using id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val title = "${snapshot.child("title").value}"
                        //set category
                        bookTv.text = title
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun deleteBook(
            context: Context,
            bookId: String,
            bookUrl: String,
            bookTitle: String
        ) {
            val TAG = "DELETE_BOOK_TAG"
            Log.d(TAG, "deleteBook: deleting")

            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: deleting from fire storage")

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: deleting from storage")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Deleted successfully")
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .addOnFailureListener { f ->
                            progressDialog.dismiss()
                            Log.d(TAG, "deleteBook: Deleted failed: ${f.message}")
                            Toast.makeText(
                                context,
                                "Deleted failed: ${f.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener { f ->
                    progressDialog.dismiss()
                    Log.d(TAG, "deleteBook: Deleted failed: ${f.message}")
                    Toast.makeText(context, "Deleted failed: ${f.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }

        fun incrementChapterView(chapterId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Chapters")
            ref.child(chapterId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var viewCount = "${snapshot.child("viewCount").value}"
                        if (viewCount == "" || viewCount == "null") {
                            viewCount = "0"
                        }

                        val newViewCount = viewCount.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap["viewCount"] = newViewCount

                        val refView = FirebaseDatabase.getInstance().getReference("Chapters")
                        refView.child(chapterId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun incrementDownloadCount(chapterId: String) {
            val ref = FirebaseDatabase.getInstance().getReference("Chapters")
            ref.child(chapterId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var downloadCount = "${snapshot.child("downloadCount").value}"
                        if (downloadCount == "" || downloadCount == "null") {
                            downloadCount = "0"
                        }

                        val newDownloadCount = downloadCount.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap["downloadCount"] = newDownloadCount

                        val refView = FirebaseDatabase.getInstance().getReference("Chapters")
                        refView.child(chapterId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun sendNotification(context: AddChapterActivity, title: String, message: String) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val channelDescription = "My Channel Description"
            val notificationId = 1

            // Tạo đối tượng NotificationManager
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Kiểm tra phiên bản Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Tạo Notification Channel nếu phiên bản Android >= Oreo
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = channelDescription
                    enableLights(true)
                    lightColor = Color.RED
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Tạo Intent để xử lý sự kiện khi người dùng nhấn vào thông báo
            val intent = Intent(context, NotifyActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Thêm cờ FLAG_IMMUTABLE vào đây
            )

            // Tạo đối tượng NotificationCompat.Builder
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.book_logo3)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Mức độ ưu tiên
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // Đặt Intent xử lý sự kiện

            notificationManager.notify(notificationId, builder.build())
        }

        val BASE_URL = "https://fcm.googleapis.com/fcm/send"
        val SERVER_KEY = "key=AAAA4ERbNzo:APA91bHgLahVSfGeddPY58gzkiVvaePKtAena9nHiTBMq7ZyxZT6eb7g_TaZS-Ry_vgx-5B5bI_yZrGkF2nZMNazdZeJDHEwHlHxY5-KTFZ2e0dFzNdYm2uJKc4yH3u3qU4sv7kogIER"

        fun pushNotification(context: Context, token:String, title:String, message:String) {
            val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val queue: RequestQueue = Volley.newRequestQueue(context)

            try {
                val json = JSONObject()
                json.put("to", token)
                val notification = JSONObject()
                notification.put("title", title)
                notification.put("body", message)
                json.put("notification", notification)

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.POST, BASE_URL, json,
                    Response.Listener<JSONObject> { response ->
                        Log.d(TAG, "FCM: response: $response")
                    },
                    Response.ErrorListener { error ->
                        Log.d(TAG, "FCM: failed: $error")
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["Content-Type"] = "application/json"
                        params["Authorization"] = SERVER_KEY
                        return params
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }




    }

}