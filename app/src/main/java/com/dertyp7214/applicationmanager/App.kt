/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.content.edit
import com.dertyp7214.applicationmanager.helpers.Network
import com.dertyp7214.applicationmanager.helpers.Network.Companion.isNetworkAvailable
import com.dertyp7214.applicationmanager.screens.MainActivity
import com.dertyp7214.applicationmanager.screens.Splash
import com.dertyp7214.logs.helpers.Logger
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.fede987.statusbaralert.StatusBarAlert

class App : Application() {

    private var thread: Thread? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        context = applicationContext
        Logger.init(this)
        PRDownloader.initialize(applicationContext, config)

        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                context = activity
                handleTheme(activity)
                handleNetwork(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                context = activity
                handleTheme(activity)
                handleNetwork(activity)
            }

            override fun onActivityResumed(activity: Activity) {
                context = activity
                handleTheme(activity)
                handleNetwork(activity)
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun checkDarkMode() {
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit {
            putBoolean(
                "dark_mode",
                (getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode == UiModeManager.MODE_NIGHT_YES
            )
        }
    }

    private fun handleTheme(activity: Activity?) {
        try {
            checkDarkMode()
            val typedArrayDark = obtainStyledAttributes(
                R.style.AppTheme_Splash,
                intArrayOf(android.R.attr.windowBackground)
            )
            val windowBackgroundDark = typedArrayDark.getColor(0, Color.DKGRAY)
            typedArrayDark.recycle()

            val darkMode = getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("dark_mode", true)
            if (activity is Splash || darkMode)
                activity!!.window.navigationBarColor = windowBackgroundDark
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity!!.window.navigationBarColor = Color.parseColor("#FAFAFA")
                } else
                    activity!!.window.navigationBarColor = Color.LTGRAY
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    activity.window.navigationBarDividerColor = Color.LTGRAY
            }
            when (activity) {
                is MainActivity -> activity.setTheme(if (darkMode) R.style.AppTheme_Dark_NoActionBar else R.style.AppTheme_NoActionBar)
                !is Splash -> activity.setTheme(if (darkMode) R.style.AppTheme_Dark else R.style.AppTheme)
                else -> {
                    activity.window.statusBarColor = windowBackgroundDark
                    activity.setTheme(R.style.AppTheme_Splash)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun handleNetwork(activity: Activity?) {
        try {
            if (thread != null)
                if (thread!!.isAlive)
                    thread!!.interrupt()
            thread = Thread {
                if (!isNetworkAvailable(activity!!)) {
                    try {
                        Network.disabled = true
                        activity.runOnUiThread {
                            StatusBarAlert.Builder(activity)
                                .autoHide(false)
                                .showProgress(true)
                                .withText("No Connection")
                                .withAlertColor(android.R.color.holo_red_light)
                                .build()
                        }
                    } catch (e: Exception) {
                    }
                } else {
                    try {
                        Network.disabled = false
                        activity.runOnUiThread {
                            StatusBarAlert.hide(activity, Runnable {})
                        }
                    } catch (e: Exception) {
                    }
                }
                try {
                    var last = isNetworkAvailable(activity)
                    while (!thread!!.isInterrupted) {
                        Thread.sleep(500)
                        if (last != isNetworkAvailable(activity)) {
                            last = isNetworkAvailable(activity)
                            handleNetwork(activity)
                        }
                    }
                } catch (e: Exception) {
                }
            }
            thread!!.start()
        } catch (e: Exception) {
        }
    }
}
