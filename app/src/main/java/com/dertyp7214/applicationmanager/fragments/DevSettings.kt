/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.fragments

import android.animation.ValueAnimator
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.core.content.edit
import androidx.core.view.setPadding
import androidx.core.widget.NestedScrollView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.core.contains
import com.dertyp7214.applicationmanager.core.find
import com.dertyp7214.applicationmanager.core.forEach
import com.dertyp7214.applicationmanager.screens.MainActivity
import com.dertyp7214.applicationmanager.themes.ThemePreviewScreen
import com.dertyp7214.logs.helpers.Ui.Companion.getAttrColor
import com.dertyp7214.preferencesplus.core.dp
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.json.JSONArray

class DevSettings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.developer_preferences, rootKey)

        val activity = requireActivity()

        val darkMode: SwitchPreference = findPreference("dark_mode") as SwitchPreference
        val devSettingsEnabled: SwitchPreference = findPreference("dev_settings_enabled") as SwitchPreference
        val colorStyle: Preference = findPreference("color_style")
        val repositories: Preference = findPreference("repos")

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

        repositories.setOnPreferenceClickListener {
            val prefs = context!!.getSharedPreferences("settings", MODE_PRIVATE)
            val repos = JSONArray(prefs.getString("repos", "[]"))
            BottomSheet(repos) { text, delete ->
                val current = JSONArray(prefs.getString("repos", "[]"))
                if (delete) {
                    prefs.edit {
                        current.remove(current.find(text))
                        putString("repos", current.toString())
                    }
                } else {
                    prefs.edit {
                        Log.d("ADD BEFORE", "$current")
                        current.put(text)
                        Log.d("ADD AFTER", "$current $text")
                        putString("repos", current.toString())
                        Log.d("ADD AFTER AFTER", prefs.getString("repos", "[]"))
                    }
                }
            }.show(activity.supportFragmentManager, "")
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = getString(R.string.dev_settings)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}

class BottomSheet(val repos: JSONArray, val onPress: (string: String, delete: Boolean) -> Unit) :
    BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return NestedScrollView(context!!).apply {
            addView(LinearLayout(context).apply {
                orientation = VERTICAL
                setPadding(5.dp(context))
                setBackgroundColor(getAttrColor(context, android.R.attr.windowBackground))
                lateinit var root: LinearLayout
                addView(LinearLayout(context).apply {
                    orientation = VERTICAL
                    root = this
                    repos.forEach { name, index ->
                        addView(getView(name, root))
                    }
                })
                addView(LinearLayout(context).apply {
                    orientation = HORIZONTAL
                    lateinit var editText: EditText
                    addView(EditText(context).apply {
                        hint = getString(R.string.repo_name)
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1F)
                        editText = this
                    })
                    addView(Button(context).apply {
                        text = getString(R.string.add)
                        val typedArrayDark = activity!!.obtainStyledAttributes(
                            intArrayOf(android.R.attr.selectableItemBackground)
                        )
                        background = typedArrayDark.getDrawable(0)
                        typedArrayDark.recycle()
                        setOnClickListener {
                            if (editText.text.isNotEmpty() && !repos.contains(editText.text.toString())) {
                                onPress(editText.text.toString(), false)
                                ChangeBounds().apply {
                                    startDelay = 0
                                    interpolator = AccelerateDecelerateInterpolator()
                                    duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                                    TransitionManager.beginDelayedTransition(root, this)
                                }
                                root.addView(getView(editText.text.toString(), root))
                                editText.setText("")
                            } else {
                                Toast.makeText(
                                    context,
                                    if (editText.text.isEmpty()) R.string.empty else R.string.already_registered,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    })
                })
            })
        }
    }

    private fun getView(name: String, root: LinearLayout): LinearLayout {
        return LinearLayout(context).apply {
            orientation = HORIZONTAL
            val layout = this
            addView(TextView(context).apply {
                text = name
                maxLines = 1
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1F)
            })
            addView(Button(context).apply {
                text = getString(R.string.delete)
                val typedArrayDark = activity!!.obtainStyledAttributes(
                    intArrayOf(android.R.attr.selectableItemBackground)
                )
                background = typedArrayDark.getDrawable(0)
                typedArrayDark.recycle()
                setOnClickListener {
                    onPress(name, true)
                    ChangeBounds().apply {
                        startDelay = 0
                        interpolator = AccelerateDecelerateInterpolator()
                        duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                        TransitionManager.beginDelayedTransition(root, this)
                    }
                    root.removeView(layout)
                }
            })
        }
    }
}