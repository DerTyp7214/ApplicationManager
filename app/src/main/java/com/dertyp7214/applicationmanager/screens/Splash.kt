/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.screens

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dertyp7214.applicationmanager.R
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*

class Splash : AppCompatActivity() {

    private val PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        checkPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    finish()
                }
            }
            run()
        }
    }

    private fun run() {
        val max = 1F
        val animator = ValueAnimator.ofFloat(0F, max)
        animator.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            imageView.scaleX = value * (1F / max)
            imageView.scaleY = value * (1F / max)
        }
        animator.interpolator = OvershootInterpolator()
        animator.addListener({
            Handler().postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, resources.getInteger(android.R.integer.config_longAnimTime).toLong())
        })
        animator.start()
    }

    private fun checkPermissions() {
        ActivityCompat.requestPermissions(
            this,
            permissions().toTypedArray(),
            PERMISSIONS
        )
    }

    private fun permissions(): List<String> {
        return ArrayList(
            Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }
}
