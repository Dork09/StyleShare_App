/**
 * מטרת הקובץ:
 * Activity ראשי:
 * - מחזיק NavHost
 * - מחבר BottomNavigation ל-Navigation Component
 */
package com.example.styleshare

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.styleshare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /** נקודת כניסה ראשית */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)
        
        // --- Added: Handle FAB click to navigate to create look ---
        binding.fabAddLook.setOnClickListener {
            navController.navigate(R.id.createLookFragment)
        }

        // --- Added: Hide Bottom Navigation on Auth screens so user can't navigate away before logging in ---
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNav.visibility = View.GONE
                    binding.fabAddLook.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.fabAddLook.visibility = View.VISIBLE
                }
            }
        }
    }
}
