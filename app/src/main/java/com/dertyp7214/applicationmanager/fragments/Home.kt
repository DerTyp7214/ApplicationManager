/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager.GET_META_DATA
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.dertyp7214.applicationmanager.BuildConfig
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.helpers.*
import com.dertyp7214.logs.helpers.Logger
import com.dertyp7214.preferencesplus.core.dp
import com.dertyp7214.preferencesplus.core.setMargins
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class Home : Fragment() {

    private val url = "https://api.github.com/repos/DerTyp7214/ApplicationManager/releases"
    private var prerelease = false
    private var magiskFound: Boolean = false
    private lateinit var activity: Activity
    private lateinit var txtLatestVersion: TextView
    private lateinit var txtCurrentVersion: TextView
    private lateinit var txtUpdate: TextView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var btnUpdate: Button
    private lateinit var cardVersions: CardView
    private lateinit var magiskCard: CardView
    private lateinit var magiskIcon: Drawable
    private lateinit var magiskObject: JSONObject

    companion object {
        private var release = ""
    }

    operator fun JSONArray.iterator(): Iterator<JSONObject> =
        (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.home, container, false)

        activity = getActivity()!!
        activity.title = getString(R.string.home)

        prerelease = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("beta_channel", false)

        txtLatestVersion = v.findViewById(R.id.txt_latest)
        txtCurrentVersion = v.findViewById(R.id.txt_current)
        txtUpdate = v.findViewById(R.id.txt_update)
        refreshLayout = v.findViewById(R.id.refreshLayout)
        btnUpdate = v.findViewById(R.id.btn_install)
        cardVersions = v.findViewById(R.id.card_versions)
        magiskCard = v.findViewById(R.id.magisk_card)

        Thread {
            val pair = RootChecker.getInstance(activity.application).checkForMagisk()
            magiskFound = pair.first
            if (magiskFound) {
                try {
                    magiskObject = pair.second
                    val info =
                        context!!.packageManager.getApplicationInfo(
                            magiskObject.getString("packageName"),
                            GET_META_DATA
                        )
                    magiskIcon = info.loadIcon(context!!.packageManager)
                    activity.runOnUiThread {
                        ChangeBounds().apply {
                            startDelay = 0
                            interpolator = AccelerateDecelerateInterpolator()
                            duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                            TransitionManager.beginDelayedTransition(magiskCard, this)
                        }
                        magiskCard.apply {
                            if (layoutParams != null) layoutParams.height = WRAP_CONTENT
                            else layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                            val margin = 7.dp(activity)
                            setMargins(margin, margin, margin, margin)
                            requestLayout()
                        }
                        magiskCard.findViewById<ViewGroup>(R.id.layout_magisk).setOnClickListener {
                            startActivity(activity.packageManager.getLaunchIntentForPackage(info.packageName))
                        }
                        v.findViewById<TextView>(R.id.magisk_app_name).text = info.loadLabel(context!!.packageManager)
                        v.findViewById<TextView>(R.id.txt_magisk_version).text =
                                "${getString(R.string.version)}: ${magiskObject.getString("versionName")} (${magiskObject.getString(
                                    "versionCode"
                                )})"
                        v.findViewById<TextView>(R.id.txt_magisk_packagename).text =
                                "${getString(R.string.package_name)}: ${info.packageName}"
                        v.findViewById<ImageView>(R.id.magisk_icon).setImageDrawable(magiskIcon)
                    }
                } catch (e: Exception) {
                    Logger.log(Logger.Companion.Type.ERROR, "checkForMagisk", Log.getStackTraceString(e))
                }
            }
        }.start()

        refreshLayout.setOnRefreshListener {
            loadVersion(true)
        }

        loadVersion()

        return v
    }

    @SuppressLint("SetTextI18n")
    private fun loadVersion(refreshing: Boolean = false) {
        var changeBounds = ChangeBounds()
        changeBounds.startDelay = 0
        changeBounds.interpolator = AccelerateDecelerateInterpolator()
        changeBounds.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        TransitionManager.beginDelayedTransition(cardVersions, changeBounds)
        TransitionManager.beginDelayedTransition(magiskCard, changeBounds)
        btnUpdate.visibility = INVISIBLE
        btnUpdate.apply {
            if (ColorUtils.calculateLuminance(backgroundTintList?.defaultColor ?: 0) > 0.5F) {
                setTextColor(Color.WHITE)
            } else {
                setTextColor(Color.BLACK)
            }
        }
        txtLatestVersion.visibility = GONE
        refreshLayout.isRefreshing = false
        txtUpdate.text = "${getString(R.string.checking_for_updates)}..."
        txtCurrentVersion.text =
                "${getString(R.string.currentVersion)}: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

        try {
            Handler().postDelayed({
                Thread {
                    if (release.isEmpty() || refreshing)
                        release = Network.getJSONObject(url, Config.API_KEY)
                    val json = JSONArray(release).iterator().asSequence().find {
                        (prerelease && it.getBoolean("prerelease")) || !it.getBoolean("prerelease")
                    }!!
                    val version = json.getString("tag_name")
                    val apk = json.getJSONArray("assets")?.getJSONObject(0)?.getString("browser_download_url")

                    if (!this@Home.isDetached) {
                        activity.runOnUiThread {
                            try {
                                changeBounds = ChangeBounds()
                                changeBounds.startDelay = 0
                                changeBounds.interpolator = AccelerateDecelerateInterpolator()
                                changeBounds.duration =
                                        resources.getInteger(android.R.integer.config_longAnimTime).toLong()
                                TransitionManager.beginDelayedTransition(cardVersions, changeBounds)
                                TransitionManager.beginDelayedTransition(magiskCard, changeBounds)
                                btnUpdate.visibility = VISIBLE
                                txtLatestVersion.visibility = VISIBLE
                                txtLatestVersion.text = "${getString(R.string.latestVersion)}: $version"
                                refreshLayout.isRefreshing = false
                                val update = Comparators.compareVersion(version, BuildConfig.VERSION_NAME) == 1
                                Logger.log(
                                    Logger.Companion.Type.DEBUG,
                                    "loadVersion",
                                    "latestVersion: $version, currentVersion: ${BuildConfig.VERSION_NAME}, update: $update"
                                )
                                txtUpdate.text =
                                        "${getString(R.string.app_name)} ${getString(if (update && apk != null) R.string.update_available else R.string.up_to_date)}"
                                if (update && apk != null) {
                                    btnUpdate.text = getString(R.string.update)
                                    btnUpdate.setOnClickListener {
                                        val path =
                                            File(Environment.getExternalStorageDirectory(), ".application_manager")
                                        if (!path.exists()) path.mkdirs()
                                        val progressDialog =
                                            ProgressDialog.show(
                                                activity,
                                                "",
                                                "${getString(R.string.download_update)}(0%)"
                                            )
                                        Network.downloadFile(apk, path,
                                            "${getString(R.string.app_name).toLowerCase().replace(" ", "_")}-" +
                                                    "${version.replace(".", "_")}.apk", activity, { progress, bytes ->
                                                progressDialog.setMessage(
                                                    "${getString(R.string.download_update)}(${
                                                    if (progress > 0) "$progress%"
                                                    else Network.humanReadableByteCount(bytes, true)
                                                    })"
                                                )
                                            }, { file, success ->
                                                progressDialog.dismiss()
                                                if (!success)
                                                    Packages.install(activity, file)
                                                else
                                                    Toast.makeText(
                                                        activity,
                                                        getString(R.string.error_apk),
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                            })
                                    }
                                } else {
                                    btnUpdate.text = getString(R.string.uninstall)
                                    btnUpdate.setOnClickListener {
                                        Intent(Intent.ACTION_DELETE).apply {
                                            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                            startActivity(this)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Logger.log(Logger.Companion.Type.ERROR, "loadVersion", Log.getStackTraceString(e))
                            }
                        }
                    }
                }.start()
            }, 1000)
        } catch (e: Exception) {
            Logger.log(Logger.Companion.Type.ERROR, "loadVersion", Log.getStackTraceString(e))
        }
    }
}