package com.example.mobileapplicationoku

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminBottomNavActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_bottom_nav)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        val navController = findNavController(R.id.fragment2)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment,R.id.mapFragment,R.id.approveListFragment,
            R.id.approveDetailsFragment, R.id.appFacilitiesDetailsFragment,R.id.serviceHomeFragment, R.id.handsignFragment,
            R.id.parttimeDetailsFragment,R.id.messageDetailFragment,R.id.messageFragment,
            R.id.appFacilitiesDetailsFragment,R.id.applyFragment,R.id.caregiverFragment, R.id.event, R.id.eventDetail,
            R.id.nonGovEvent, R.id.nonGovEventDetail, R.id.registerEvent))
        setupActionBarWithNavController(navController,appBarConfiguration)

        bottomNavigationView.setupWithNavController(navController)
    }
}