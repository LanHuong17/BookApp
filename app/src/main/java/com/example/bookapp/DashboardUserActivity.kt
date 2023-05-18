package com.example.bookapp

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.example.bookapp.UserActivity.CategoryFragment
import com.example.bookapp.UserActivity.FavoriteFragment
import com.example.bookapp.UserActivity.HomeFragment
import com.example.bookapp.UserActivity.ProfileFragment
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.example.bookapp.databinding.ActivityDashboardUserBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException

class DashboardUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardUserBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val HomeFragment = HomeFragment()
    val CategoryFragment = CategoryFragment()
    val FavoriteFragment = FavoriteFragment()
    val ProfileFragment = ProfileFragment()
    var media: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFragment)

        firebaseAuth = FirebaseAuth.getInstance()

        checkUser()

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> replaceFragment(HomeFragment)
                R.id.category -> replaceFragment(CategoryFragment)
                R.id.favorite -> replaceFragment(FavoriteFragment)
                R.id.profile -> replaceFragment(ProfileFragment())
                else -> {}
            }
            
            true
        }

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.musicBtn.setOnCheckedChangeListener{_, isChecked ->
            if(isChecked){
                playAudio()
                onSwitchChanged(true)
            } else{
                pauseAudio()
                onSwitchChanged(false)
            }
        }
        setSwitchState()

    }

    private fun pauseAudio() {
        if (media == null) {
            media = MediaPlayer.create(this, R.raw.peaceful_hike)
            media?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            media!!.pause()
        } else{
            media!!.pause()
        }
    }

    private fun playAudio() {
        if (media == null) {
            media = MediaPlayer.create(this, R.raw.peaceful_hike)
            media?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                media?.apply {
                    if (!isPlaying) {
                        start()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else{
            media?.start()
        }
    }

    private fun onSwitchChanged(isChecked: Boolean) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("SWITCH_STATE", isChecked)
        editor.apply()
    }

    private fun setSwitchState() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val switchState = sharedPreferences.getBoolean("SWITCH_STATE", false)
        binding.musicBtn.isChecked = switchState
    }




    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            binding.tvSubTitle.text = "Not logged in"
        } else {
            val email = firebaseUser.email
            val name = firebaseUser.displayName
            binding.tvSubTitle.text = email
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            if (fragment.isAdded) {
                show(fragment)
            } else {
                add(R.id.frame_layout, fragment)
            }

            supportFragmentManager.fragments.forEach {
                if (it != fragment && it.isAdded) {
                    hide(it)
                }
            }
        }.commit()
    }
}