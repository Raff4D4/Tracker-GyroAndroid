package com.example.tracker_gyro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tracker_gyro.core.navigation.CapsuleBottomBar
import com.example.tracker_gyro.core.ui.theme.GlassTheme
import com.example.tracker_gyro.features.airscroll.AirScrollScreen
import com.example.tracker_gyro.features.airscroll.AirScrollViewModel
import com.example.tracker_gyro.features.ambient.AmbientScreen
import com.example.tracker_gyro.features.ambient.AmbientViewModel
import com.example.tracker_gyro.features.bubblelevel.BubbleLevelScreen
import com.example.tracker_gyro.features.bubblelevel.BubbleLevelViewModel
import com.example.tracker_gyro.features.parallax.ParallaxScreen
import com.example.tracker_gyro.features.parallax.ParallaxViewModel

class MainActivity : ComponentActivity() {
    private val parallaxViewModel: ParallaxViewModel by viewModels()
    private val airScrollViewModel: AirScrollViewModel by viewModels()
    private val bubbleLevelViewModel: BubbleLevelViewModel by viewModels()
    private val ambientViewModel: AmbientViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permissions)
        }
        
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        ) == 1
        
        if (!accessibilityEnabled) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        setContent {
            GlassTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { CapsuleBottomBar(navController) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "parallax") {
                            composable("parallax") {
                                ParallaxScreen(parallaxViewModel)
                            }
                            composable("airScroll") {
                                AirScrollScreen(airScrollViewModel)
                            }
                            composable("bubbleLevel") {
                                BubbleLevelScreen(bubbleLevelViewModel)
                            }
                            composable("ambient") {
                                AmbientScreen(ambientViewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.tracker_gyro.service.ForegroundGate.isActive = true
    }

    override fun onPause() {
        super.onPause()
        com.example.tracker_gyro.service.ForegroundGate.isActive = false
    }
}
