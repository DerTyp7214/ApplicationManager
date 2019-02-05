/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.applicationmanager.helpers

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.logs.helpers.Logger
import com.dertyp7214.preferencesplus.components.ColorUtil.Companion.getDominantColor
import org.json.JSONObject

class RootChecker private constructor(private val application: Application) {

    private var packages: MutableList<ApplicationInfo>? = null
        get() {
            if (field == null) {
                field = application.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }
            return field
        }

    private var json: JSONObject? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: RootChecker? = null

        fun getInstance(application: Application): RootChecker {
            if (instance == null) instance = RootChecker(application)
            return instance!!
        }
    }

    fun checkForMagisk(): Pair<Boolean, JSONObject> {
        var ret = false
        val obj =
            JSONObject("""{"found": false, "sure": false, "versionName": "", "versionCode": "", "packageName": ""}""")

        if (json != null) return Pair(json!!.getBoolean("found"), json!!)
        packages!!.forEach {
            if (isColor(
                    getDominantColor(getBitmapFromDrawable(it.loadIcon(application.packageManager))),
                    Color.parseColor("#FF00AF9C"),
                    10
                ) || it.packageName == "com.topjohnwu.magisk"
            ) {
                if ((it.packageName.startsWith("com.") && it.packageName.count { char -> char == '.' } >= 3) || it.packageName == "com.topjohnwu.magisk") {
                    val iconCompare = compareIcons(
                        getBitmapFromDrawable(it.loadIcon(application.packageManager)),
                        getBitmapFromDrawable(application.getDrawable(R.drawable.ic_launcher_magisk)!!)
                    )
                    if (iconCompare >= 0.85F || it.packageName == "com.topjohnwu.magisk") {
                        val versionName = application.packageManager.getPackageInfo(it.packageName, 0).versionName
                        val versionCode = application.packageManager.getPackageInfo(it.packageName, 0).versionCode
                        obj.put("sure", it.packageName == "com.topjohnwu.magisk" || iconCompare == 1F)
                        obj.put("versionName", versionName)
                        obj.put("versionCode", versionCode)
                        obj.put("packageName", it.packageName)
                        obj.put("found", true)
                        ret = true
                    }
                }
            }
        }
        json = obj
        return Pair(ret, obj)
    }

    private fun compareIcons(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        val bmp1 = Bitmap.createScaledBitmap(bitmap1, 252, 252, false)
        val bmp2 = Bitmap.createScaledBitmap(bitmap2, 252, 252, false)
        var accuracy = 0F
        if (bmp1.width == bmp2.width && bmp1.height == bmp2.height) {
            for (x in 0 until bmp1.width) {
                for (y in 0 until bmp1.height) {
                    if (isColor(bmp1.getPixel(x, y), bmp2.getPixel(x, y), 5)) {
                        accuracy += 100F / (bmp1.width * bmp1.height)
                    }
                }
            }
        }
        return (accuracy / 100F).apply {
            Logger.log(
                Logger.Companion.Type.INFO,
                "IconCompare",
                "${bitmap1.toString().replace(
                    "android.graphics.Bitmap",
                    ""
                )} and ${bitmap2.toString().replace("android.graphics.Bitmap", "")} is ${this * 100}%"
            )
        }
    }

    private fun isColor(color1: Int, color2: Int, tolerance: Int): Boolean {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        return r1 >= r2 - tolerance && r1 <= r2 + tolerance
                && g1 >= g2 - tolerance && g1 <= g2 + tolerance
                && b1 >= b2 - tolerance && b1 <= b2 + tolerance
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bmp = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }
}