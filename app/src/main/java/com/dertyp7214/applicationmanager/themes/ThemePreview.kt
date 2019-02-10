/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.themes

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dertyp7214.applicationmanager.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ThemePreview(private val title: String, val theme: Theme) : Fragment() {

    @SuppressLint("ResourceType")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.theme_preview, container, false)

        var primaryDark = theme.colorPrimaryDark
        var primary = theme.colorPrimary
        var accent = theme.colorAccent

        if (theme.resourceValue) {
            primaryDark = resources.getColor(theme.colorPrimaryDark)
            primary = resources.getColor(theme.colorPrimary)
            accent = resources.getColor(theme.colorAccent)
        }

        v.findViewById<ViewGroup>(R.id.statusbar_theme).setBackgroundColor(primaryDark)
        v.findViewById<ViewGroup>(R.id.toolbar_theme).setBackgroundColor(primary)
        v.findViewById<FloatingActionButton>(R.id.floatingActionButton).backgroundTintList =
            ColorStateList.valueOf(accent)
        v.findViewById<FloatingActionButton>(R.id.floatingActionButton).imageTintList =
            ColorStateList.valueOf(if (isColorDark(accent)) Color.WHITE else Color.BLACK)
        v.findViewById<TextView>(R.id.title_theme).text = title
        v.findViewById<TextView>(R.id.title_theme).setTextColor(if (isColorDark(primary)) Color.WHITE else Color.BLACK)

        return v
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    data class Theme(
        val colorPrimaryDark: Int,
        val colorPrimary: Int,
        val colorAccent: Int,
        val themeId: Int,
        val resourceValue: Boolean
    )
}