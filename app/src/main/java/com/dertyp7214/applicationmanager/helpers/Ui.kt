/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.content.Context
import android.graphics.Color

class Ui {
    companion object {
        fun getAttrColor(context: Context, attr: Int): Int {
            return try {
                val ta = context.obtainStyledAttributes(intArrayOf(attr))
                val colorAccent = ta.getColor(0, 0)
                ta.recycle()
                colorAccent
            } catch (e: Exception) {
                Logs.log(Logs.Companion.Type.ERROR, "getAttrColor", e.message)
                Color.RED
            }
        }
    }
}