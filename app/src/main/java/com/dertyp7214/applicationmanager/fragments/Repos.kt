/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.applicationmanager.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.adapters.RepoAdapter
import com.dertyp7214.applicationmanager.helpers.Api
import com.dertyp7214.applicationmanager.helpers.Comparators
import com.dertyp7214.applicationmanager.helpers.Packages
import com.dertyp7214.applicationmanager.helpers.RepoLoader
import com.dertyp7214.applicationmanager.props.Application

class Repos : Fragment() {

    private lateinit var api: Api
    private lateinit var activity: Activity
    private var dialog: ProgressDialog? = null

    companion object {
        var triggerAsync: (query: String) -> Unit = {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_repos, container, false)

        activity = getActivity()!!
        activity.title = getString(R.string.repos)

        api = Api(activity)

        dialog = ProgressDialog.show(activity, "", "Loading Repo")
        loadData(v)

        triggerAsync = {
            loadData(v, it)
        }

        v.findViewById<SwipeRefreshLayout>(R.id.refresh).apply {
            setOnRefreshListener {
                RepoLoader.getInstance(activity).loadRepoAsync()
                loadData(v, "") {
                    isRefreshing = false
                }
            }
        }

        return v
    }

    private fun loadData(v: View, query: String = "", finished: () -> Unit = {}) {
        Thread {
            val apps = ArrayList<Application>()
            val updates = ArrayList<Application>()
            val installed = ArrayList<Application>()
            val download = ArrayList<Application>()
            val loader = RepoLoader.getInstance(activity)
            while (loader.loading) Thread.sleep(100)
            ArrayList(loader.getRepoList(query).filter { it.latestApk.isNotEmpty() }).forEach {
                if (Packages.isPackageInstalled(it.packageName, activity.packageManager)) {
                    if (Comparators.compareVersion(
                            it.version,
                            Packages.getVersion(it.packageName, activity.packageManager)
                        ) == 1
                    ) updates.add(it)
                    else installed.add(it)
                } else download.add(it)
            }
            if (updates.isNotEmpty()) apps.add(Application.divider("Update Available"))
            apps.addAll(updates)
            if (installed.isNotEmpty()) apps.add(Application.divider("Installed"))
            apps.addAll(installed)
            if (download.isNotEmpty()) apps.add(Application.divider("Not Installed"))
            apps.addAll(download)
            activity.runOnUiThread {
                if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
                RepoAdapter(activity, v.findViewById(R.id.rv), apps)
                finished()
            }
        }.start()
    }
}
