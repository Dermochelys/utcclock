package com.dermochelys.utcclock

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import com.dermochelys.utcclock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.setPadding(0, 0, 0, 0)

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = 0
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    // Helpers

    private fun hideSystemUi() {
        val types = getTypes()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        windowInsetsController.hide(types)
    }
}

internal fun getTypes() = Type.systemBars() or Type.displayCutout() or Type.navigationBars() or Type.statusBars()
