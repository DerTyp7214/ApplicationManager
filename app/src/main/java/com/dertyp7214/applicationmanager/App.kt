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
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import com.dertyp7214.applicationmanager.helpers.Network
import com.dertyp7214.applicationmanager.helpers.Network.Companion.isNetworkAvailable
import com.dertyp7214.applicationmanager.receivers.PackageReceiver
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
        var packages: MutableList<ApplicationInfo>? = null
            get() {
                if (field == null) {
                    field = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                return field
            }

        fun reloadPackages() {
            packages = null
        }

        fun setTheme(theme: Int) {
            val prefs = context.getSharedPreferences("themes", Context.MODE_PRIVATE)
            prefs.edit {
                val themeLight = when (theme) {
                    R.style.AppTheme_Green -> R.style.AppTheme_Green
                    else -> R.style.AppTheme
                }
                val themeDark = when (theme) {
                    R.style.AppTheme_Green -> R.style.AppTheme_Green_Dark
                    else -> R.style.AppTheme_Dark
                }
                val themeLightNo = when (theme) {
                    R.style.AppTheme_Green -> R.style.AppTheme_Green_NoActionBar
                    else -> R.style.AppTheme
                }
                val themeDarkNo = when (theme) {
                    R.style.AppTheme_Green -> R.style.AppTheme_Green_Dark_NoActionBar
                    else -> R.style.AppTheme_Dark
                }
                putInt("themeLight", themeLight)
                putInt("themeDark", themeDark)
                putInt("themeLightNo", themeLightNo)
                putInt("themeDarkNo", themeDarkNo)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .build()
        context = applicationContext
        Logger.init(this)
        PRDownloader.initialize(applicationContext, config)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //checkForPackageChanges()
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL)
            intentFilter.addDataScheme("package")
            registerReceiver(PackageReceiver(), intentFilter)
        }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkForPackageChanges() {
        Thread {
            while (true) {
                val packages = packageManager.getChangedPackages(SystemClock.elapsedRealtime().toInt() - 500)
                Log.d("PACKAGES", if (packages?.packageNames != null) packages.packageNames.joinToString() else "")
                Thread.sleep(500)
            }
        }.start()
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
                is MainActivity -> activity.setTheme(getTheme(darkMode, true))
                !is Splash -> activity.setTheme(getTheme(darkMode, false))
                else -> {
                    activity.window.statusBarColor = windowBackgroundDark
                    activity.setTheme(R.style.AppTheme_Splash)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun getTheme(darkMode: Boolean, noActionBar: Boolean): Int {
        val prefs = getSharedPreferences("themes", Context.MODE_PRIVATE)
        return if (noActionBar)
            if (darkMode) prefs.getInt("themeDarkNo", R.style.AppTheme_Dark_NoActionBar) else prefs.getInt(
                "themeLightNo",
                R.style.AppTheme_NoActionBar
            )
        else if (darkMode) prefs.getInt("themeDark", R.style.AppTheme_Dark) else prefs.getInt(
            "themeLight",
            R.style.AppTheme
        )
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
