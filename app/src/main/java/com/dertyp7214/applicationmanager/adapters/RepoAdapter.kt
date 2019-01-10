/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.helpers.Network
import com.dertyp7214.applicationmanager.helpers.Packages
import com.dertyp7214.applicationmanager.props.Application
import com.dertyp7214.logs.helpers.Ui
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.il.AsyncDrawableLoader
import java.io.File

class RepoAdapter(private val activity: Activity, recyclerView: RecyclerView, private val apps: List<Application>) :
    RecyclerView.Adapter<RepoAdapter.ViewHolder>() {

    init {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoAdapter.ViewHolder {
        val v = LayoutInflater.from(activity).inflate(R.layout.repo_item, parent, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = apps.size

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    override fun onBindViewHolder(holder: RepoAdapter.ViewHolder, position: Int) {
        val application = apps[position]
        val darkMode = activity.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("dark_mode", true)

        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        val width = size.x

        holder.title.text = "${application.name} ${application.version}"
        holder.description.text = application.descriptionShort
        holder.download
            .setImageDrawable(activity.getDrawable(if (darkMode) R.drawable.ic_file_download_white_24dp else R.drawable.ic_file_download_black_24dp))

        holder.download.setOnClickListener {
            val dialog = AlertDialog.Builder(activity)
                .setTitle("Install ${application.name}")
                .setMessage("Do you want to install '${application.name}' now?")
                .setPositiveButton(R.string.install) { dialog: DialogInterface, _: Int ->
                    val path = File(Environment.getExternalStorageDirectory(), ".application_manager")
                    if (!path.exists()) path.mkdirs()
                    val progressDialog =
                        ProgressDialog.show(
                            activity,
                            "",
                            "${activity.getString(R.string.download)} ${application.name}(0%)"
                        )
                    Network.downloadFile(
                        application.latestApk,
                        path,
                        "${application.name}-${application.version}.apk",
                        { progress ->
                            progressDialog.setMessage("${activity.getString(R.string.download)} ${application.name}($progress%)")
                        },
                        { file: File, b: Boolean ->
                            progressDialog.dismiss()
                            if (!b)
                                Packages.install(activity, file)
                            else
                                Toast.makeText(
                                    activity,
                                    activity.getString(R.string.error_apk),
                                    Toast.LENGTH_LONG
                                ).show()
                        })
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.download) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
            dialog.window?.decorView?.layoutParams?.width = (width * 0.85F).toInt()
            dialog.window
                ?.setBackgroundDrawable(ColorDrawable(Ui.getAttrColor(activity, android.R.attr.windowBackground)))
        }
        holder.layout.setOnClickListener {
            val config = SpannableConfiguration.builder(activity)
                .asyncDrawableLoader(AsyncDrawableLoader.create())
                .build()
            val text = TextView(activity)
            text.setPadding(dpToPixel(10F).toInt())
            text.setLineSpacing(0F, 1.2F)
            Log.d("MARKDOWN", application.description)
            Markwon.setMarkdown(text, config, application.description)
            val dialog = AlertDialog.Builder(activity)
                .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
                .setView(text)
                .create()
            dialog.show()
            dialog.window?.decorView?.layoutParams?.width = (width * 0.85F).toInt()
            dialog.window
                ?.setBackgroundDrawable(ColorDrawable(Ui.getAttrColor(activity, android.R.attr.windowBackground)))
        }
    }

    private fun dpToPixel(dp: Float): Float {
        return dp * (activity.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun pixelsToDp(px: Float): Float {
        return px / (activity.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.txt_title)
        val description: TextView = v.findViewById(R.id.txt_description)
        val download: ImageView = v.findViewById(R.id.download)
        val layout: ViewGroup = v.findViewById(R.id.layout)
    }
}