/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.TypedValue.COMPLEX_UNIT_SP
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.core.content.edit
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.dertyp7214.applicationmanager.BuildConfig
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.preferencesplus.core.dp
import com.squareup.picasso.Picasso
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class About : Fragment() {

    private lateinit var activity: Activity
    private var darkMode: Boolean = false
    private var counter: Int = 0

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.about, container, false)

        activity = getActivity()!!
        activity.title = getString(R.string.about)

        darkMode =
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_mode", true)

        val textVersion: TextView = v.findViewById(R.id.txt_version)
        val layoutDerTyp: ViewGroup = v.findViewById(R.id.dev_dertyp)
        val imageDerTyp: ImageView = v.findViewById(R.id.profile_dertyp)
        val layout: LinearLayout = v.findViewById(R.id.libraries)
        val scrollView: ScrollView = v.findViewById(R.id.scroll)
        OverScrollDecoratorHelper.setUpOverScroll(scrollView)

        val libraries = arrayListOf(
            Library("Android Open Source Project", "Android", "https://source.android.com/"),
            Library("Over-Scroll Decor", "EverythingMe", "https://github.com/EverythingMe/overscroll-decor"),
            Library("Markwon", "Dimitry Ivanov", "https://github.com/noties/Markwon"),
            Library("Picasso", "squareup", "http://square.github.io/picasso/"),
            Library("PRDownloader", "MINDORKS", "https://github.com/MindorksOpenSource/PRDownloader"),
            Library("LogsLibrary", "DerTyp7214", "https://github.com/DerTyp7214/LogsLibrary"),
            Library("PreferencesPlus", "DerTyp7214", "https://github.com/DerTyp7214/PreferencesPlus"),
            Library("StatusBarAlert", "DerTyp7214", "https://github.com/DerTyp7214/StatusBarAlert")
        )

        textVersion.text = "${getString(R.string.version)} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        layoutDerTyp.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_github_dertyp7214))))
        }

        Picasso.get().load(getString(R.string.url_github_dertyp7214_profile_image))
            .placeholder(resources.getDrawable(R.drawable.placeholder, null).apply {
                setTint((if (darkMode) Color.WHITE else Color.BLACK))
            })
            .into(imageDerTyp)

        v.findViewById<ViewGroup>(R.id.icon).setOnClickListener {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val devSettings = preferences.getBoolean("dev_settings_enabled", false)
            if (counter < 9 && !devSettings) {
                counter++
                Thread {
                    val tmp = counter
                    Thread.sleep(3000)
                    if (counter == tmp) counter = 0
                }.start()
                if (counter in 5..8) Toast.makeText(
                    activity,
                    "${9 - counter} ${getString(R.string.steps_to_dev)}",
                    Toast.LENGTH_SHORT
                ).show()
                else if (counter == 9) {
                    preferences.edit {
                        putBoolean("dev_settings_enabled", true)
                    }
                    Toast.makeText(activity, R.string.your_now_dev, Toast.LENGTH_LONG).show()
                }
            } else if (devSettings) {
                Toast.makeText(activity, R.string.already_dev, Toast.LENGTH_LONG).show()
            }
        }

        libraries.forEach {
            layout.addView(createView(it))
        }

        return v
    }

    private fun createView(library: Library): View {
        return LinearLayout(activity).apply {
            orientation = HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(12.dp(context))
            addView(LinearLayout(activity).apply {
                orientation = VERTICAL
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                addView(TextView(activity).apply {
                    text = library.title
                    setTextSize(COMPLEX_UNIT_SP, 16F)
                })
                addView(TextView(activity).apply {
                    text = library.developer
                    setTextSize(COMPLEX_UNIT_SP, 12F)
                })
            })
            addView(ImageView(activity).apply {
                setImageDrawable(resources.getDrawable(R.drawable.ic_link_black_24dp, null))
                imageTintList = ColorStateList.valueOf(if (darkMode) Color.WHITE else Color.BLACK)
            })
            val typedArrayDark = activity.obtainStyledAttributes(
                R.style.AppTheme_Dark,
                intArrayOf(android.R.attr.selectableItemBackground)
            )
            background = typedArrayDark.getDrawable(0)
            typedArrayDark.recycle()
            setOnClickListener {
                activity.startActivity(Intent(ACTION_VIEW, Uri.parse(library.url)))
            }
        }
    }

    private data class Library(val title: String, val developer: String, val url: String)
}