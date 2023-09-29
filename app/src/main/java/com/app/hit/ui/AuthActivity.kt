package com.app.hit.ui

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.app.hit.R
import com.app.hit.ui.fragments.HomeFragment
import com.app.hit.ui.fragments.LoginFragment

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_auth)


        findViewById<Button>(R.id.btnProfile).setOnClickListener {

        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {

            val fragment = LoginFragment()

            // Begin a fragment transaction
            val fragmentManager: FragmentManager = supportFragmentManager
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()

            // Replace the fragment_container view with the new fragment
            transaction.replace(R.id.fragment_container, fragment)

            // Commit the transaction
            transaction.addToBackStack(null)
            transaction.commit()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        Log.e("count:", "count: " + supportFragmentManager.backStackEntryCount)
        if (supportFragmentManager.backStackEntryCount <= 1) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
    }
}