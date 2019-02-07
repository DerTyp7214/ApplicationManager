/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.applicationmanager.App
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.adapters.InstalledReposAdapter
import com.dertyp7214.applicationmanager.helpers.RepoLoader
import com.dertyp7214.applicationmanager.helpers.RootChecker
import com.dertyp7214.applicationmanager.props.Application

class Installed : Fragment() {

    private lateinit var activity: Activity
    private lateinit var loader: RepoLoader
    private lateinit var v: View
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val installedModules = ArrayList<Application>()

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: Installed? = null
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.installed, container, false)

        activity = getActivity()!!
        activity.title = getString(R.string.installed)

        loader = RepoLoader.getInstance(activity)
        refreshLayout = v.findViewById(R.id.refresh)

        refreshLayout.isRefreshing = true
        refreshLayout.setOnRefreshListener {
            loadInstalledModules()
        }
        loadInstalledModules()

        instance = this

        return v
    }

    override fun onStop() {
        super.onStop()
        instance = null
    }

    fun loadInstalledModules() {
        Thread {
            App.reloadPackages()
            installedModules.clear()
            loader.getRepoList("").filter { it.latestApk.isNotEmpty() }.forEach { application ->
                if (App.packages!!.any { it.packageName == application.packageName }) {
                    installedModules.add(application)
                }
            }
            RootChecker.getInstance(activity.application).checkForMagisk()
            activity.runOnUiThread {
                refreshLayout.isRefreshing = false
                InstalledReposAdapter(this, v.findViewById(R.id.rv), installedModules)
            }
        }.start()
    }
}