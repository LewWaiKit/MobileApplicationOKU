package com.example.mobileapplicationoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val navController = findNavController(R.id.fragment3)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.loginFragment,R.id.registerFragment,R.id.register2Fragment))
        setupActionBarWithNavController(navController,appBarConfiguration)
    }
}