/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.applicationmanager.BuildConfig
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.helpers.Comparators
import com.dertyp7214.applicationmanager.helpers.Config
import com.dertyp7214.applicationmanager.helpers.Network
import com.dertyp7214.applicationmanager.helpers.Packages
import com.dertyp7214.logs.helpers.Logger
import org.json.JSONObject
import java.io.File

class Home() : Fragment() {

    private lateinit var activity: Activity
    private val url = "https://api.github.com/repos/DerTyp7214/ApplicationManager/releases/latest"
    private lateinit var txtLatestVersion: TextView
    private lateinit var txtCurrentVersion: TextView
    private lateinit var txtUpdate: TextView
    private lateinit var cardUpdate: CardView
    private lateinit var layoutUpdate: LinearLayout
    private lateinit var refreshLayout: SwipeRefreshLayout

    constructor(activity: Activity) : this() {
        this.activity = activity
    }

    companion object {
        private var release = ""
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.home, container, false)

        txtLatestVersion = v.findViewById(R.id.txt_latest)
        txtCurrentVersion = v.findViewById(R.id.txt_current)
        txtUpdate = v.findViewById(R.id.txt_update)
        cardUpdate = v.findViewById(R.id.card_update)
        layoutUpdate = v.findViewById(R.id.layoutUpdate)
        refreshLayout = v.findViewById(R.id.refreshLayout)
        cardUpdate.visibility = GONE

        refreshLayout.setOnRefreshListener {
            loadVersion(true)
        }
        txtCurrentVersion.text = "${getString(R.string.currentVersion)}: ${BuildConfig.VERSION_NAME}"

        loadVersion()

        return v
    }

    @SuppressLint("SetTextI18n")
    private fun loadVersion(refreshing: Boolean = false) {
        txtLatestVersion.text = "${getString(R.string.latestVersion)}: ${getString(R.string.loading)}..."

        try {
            Thread {
                if (release.isEmpty() || refreshing)
                    release = Network.getJSONObject(url, Config.API_KEY)
                val json = JSONObject(release)
                val version = json.getString("tag_name")
                val apk = json.getJSONArray("assets")?.getJSONObject(0)?.getString("browser_download_url")

                activity.runOnUiThread {
                    txtLatestVersion.text = "${getString(R.string.latestVersion)}: $version"
                    refreshLayout.isRefreshing = false
                    val update = Comparators.compareVersion(version, BuildConfig.VERSION_NAME) == 1
                    Logger.log(
                        Logger.Companion.Type.DEBUG,
                        "loadVersion",
                        "latestVersion: $version, currentVersion: ${BuildConfig.VERSION_NAME}, update: $update"
                    )
                    if (update && apk != null) {
                        txtUpdate.text = "${getString(R.string.update)} ${getString(R.string.app_name)}($version)"
                        cardUpdate.visibility = View.VISIBLE
                        layoutUpdate.setOnClickListener {
                            val path = File(Environment.getExternalStorageDirectory(), ".application_manager")
                            if (!path.exists()) path.mkdirs()
                            val progressDialog =
                                ProgressDialog.show(activity, "", "${getString(R.string.download_update)}(0%)")
                            Network.downloadFile(apk, path,
                                "${getString(R.string.app_name).toLowerCase().replace(" ", "_")}-" +
                                        "${version.replace(".", "_")}.apk", { progress ->
                                    progressDialog.setMessage("${getString(R.string.download_update)}($progress%)")
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
                    } else
                        cardUpdate.visibility = GONE
                }
            }.start()
        } catch (e: Exception) {
            Logger.log(Logger.Companion.Type.ERROR, "loadVersion", Log.getStackTraceString(e))
        }
    }
}