package com.dermochelys.utcclock

import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.dermochelys.utcclock.databinding.ActivityContentBinding
import com.dermochelys.utcclock.view.common.hideSystemUi
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Activity : AppCompatActivity() {
    private lateinit var binding: ActivityContentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityContentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureInsets()
    }

    override fun onResume() {
        super.onResume()
        window.hideSystemUi()
    }

    // Helpers

    private fun configureInsets() {
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
}
