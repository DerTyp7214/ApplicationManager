/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.adapters.RepoAdapter
import com.dertyp7214.applicationmanager.helpers.Api

class Repos() : Fragment() {

    private lateinit var activity: Activity
    private lateinit var api: Api

    constructor(activity: Activity) : this() {
        this.activity = activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_repos, container, false)

        api = Api(activity)

        val dialog = ProgressDialog.show(activity, "", "Loading Repo")
        Thread {
            val apps = api.loadApplications(api.getRepos(""))
            activity.runOnUiThread {
                if (dialog.isShowing) dialog.dismiss()
                RepoAdapter(activity, v.findViewById(R.id.rv), apps)
            }
        }.start()

        return v
    }
}
