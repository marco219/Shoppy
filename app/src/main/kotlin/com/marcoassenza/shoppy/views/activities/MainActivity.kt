package com.marcoassenza.shoppy.views.activities

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.marcoassenza.shoppy.R
import com.marcoassenza.shoppy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
    }

    companion object {
        fun mainFabCustomizer(
            activity: Activity,
            textResourceId: Int,
            iconResourceId: Int,
            onClickAction: () -> Unit
        ) {
            val context = activity.applicationContext
            val fab = activity.findViewById<ExtendedFloatingActionButton>(R.id.fab)
            fab?.apply {
                text = context.getString(textResourceId)
                icon = ResourcesCompat.getDrawable(context.resources, iconResourceId, null)

                setOnClickListener {
                    onClickAction()
                }
            }?.show()
        }
    }
}