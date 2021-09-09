package com.example.mobileapplicationoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController


class MainActivity : AppCompatActivity() {
    /*private var backPressedTime = 0L*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val navController = findNavController(R.id.fragment3)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.loginFragment,R.id.registerFragment,R.id.register2Fragment))
        setupActionBarWithNavController(navController,appBarConfiguration)
    }

   /* override fun onBackPressed() {
        super.onBackPressed()
        if(backPressedTime + 2000> System.currentTimeMillis()){
            super.onBackPressed()
        }else{
            Toast.makeText(applicationContext, "Press back again to exit",
                Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }*/
}