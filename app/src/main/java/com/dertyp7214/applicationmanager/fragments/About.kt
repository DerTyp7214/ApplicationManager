/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dertyp7214.applicationmanager.BuildConfig
import com.dertyp7214.applicationmanager.R
import com.squareup.picasso.Picasso

class About : Fragment() {

    private lateinit var activity: Activity

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.about, container, false)

        activity = getActivity()!!
        activity.title = getString(R.string.home)

        val textVersion: TextView = v.findViewById(R.id.txt_version)
        val layoutDerTyp: ViewGroup = v.findViewById(R.id.dev_dertyp)
        val imageDerTyp: ImageView = v.findViewById(R.id.profile_dertyp)

        textVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        layoutDerTyp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github_dertyp7214))))
        }

        Picasso.get().load(getString(R.string.url_github_dertyp7214_profile_image)).into(imageDerTyp)

        return v
    }
}