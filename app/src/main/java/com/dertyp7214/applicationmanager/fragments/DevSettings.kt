/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.screens.MainActivity
import com.dertyp7214.applicationmanager.themes.ThemePreviewScreen

class DevSettings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.developer_preferences, rootKey)

        val activity = requireActivity()

        val darkMode: SwitchPreference = findPreference("dark_mode") as SwitchPreference
        val devSettingsEnabled: SwitchPreference = findPreference("dev_settings_enabled") as SwitchPreference
        val colorStyle: Preference = findPreference("color_style")

        darkMode.setOnPreferenceChangeListener { _, _ ->
            ValueAnimator.ofFloat(0F, 1F).apply {
                duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                addUpdateListener {
                    if (it.animatedValue as Float == 1F) {
                        activity.startActivity(Intent(activity, MainActivity::class.java).apply {
                            putExtra("fragment", R.id.nav_dev_settings)
                        })
                        activity.finish()
                    }
                }
                start()
            }
            true
        }

        devSettingsEnabled.setOnPreferenceChangeListener { _, _ ->
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            true
        }

        colorStyle.setOnPreferenceClickListener {
            startActivity(Intent(activity, ThemePreviewScreen::class.java))
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = getString(R.string.dev_settings)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}