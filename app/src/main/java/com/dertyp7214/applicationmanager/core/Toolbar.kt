/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.core

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils

fun Toolbar.setToolbarColor(@ColorInt toolbarColor: Int) {
    val tintColor = if (isDark(toolbarColor)) Color.WHITE else Color.BLACK
    backgroundTintList = ColorStateList.valueOf(toolbarColor)
    for (imageButton in findChildrenByClass(ImageView::class.java, this)) {
        val drawable = imageButton.drawable
        drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
        imageButton.setImageDrawable(drawable)
    }
    for (textView in findChildrenByClass(TextView::class.java, this)) {
        textView.setTextColor(tintColor)
        textView.setHintTextColor(tintColor)
    }
}

private fun <V : View> findChildrenByClass(clazz: Class<V>, vararg viewGroups: ViewGroup): Collection<V> {
    val collection = ArrayList<V>()
    for (viewGroup in viewGroups)
        collection.addAll(gatherChildrenByClass(viewGroup, clazz, ArrayList()))
    return collection
}

private fun <V : View> gatherChildrenByClass(
    viewGroup: ViewGroup,
    clazz: Class<V>,
    childrenFound: MutableCollection<V>
): Collection<V> {

    for (i in 0 until viewGroup.childCount) {
        val child = viewGroup.getChildAt(i)
        if (clazz.isAssignableFrom(child.javaClass)) {
            childrenFound.add(child as V)
        }
        if (child is ViewGroup) {
            gatherChildrenByClass(child, clazz, childrenFound)
        }
    }

    return childrenFound
}

private fun isDark(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) < 0.5
}