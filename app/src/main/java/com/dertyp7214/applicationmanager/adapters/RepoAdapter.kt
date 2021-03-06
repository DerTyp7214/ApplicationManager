/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.adapters

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setPadding
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.fragments.Repos
import com.dertyp7214.applicationmanager.helpers.Network
import com.dertyp7214.applicationmanager.helpers.Packages
import com.dertyp7214.applicationmanager.props.Application
import com.dertyp7214.logs.helpers.Logger
import com.dertyp7214.logs.helpers.Ui
import com.dertyp7214.preferencesplus.core.dp
import ru.noties.markwon.Markwon
import ru.noties.markwon.SpannableConfiguration
import ru.noties.markwon.il.AsyncDrawableLoader
import java.io.File

class RepoAdapter(private val fragment: Repos, recyclerView: RecyclerView, private val apps: List<Application>) :
    RecyclerView.Adapter<RepoAdapter.ViewHolder>() {

    private val activity = fragment.activity!!

    init {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoAdapter.ViewHolder {
        return when (viewType) {
            0 -> {
                ViewHolder(LayoutInflater.from(activity).inflate(R.layout.divider_item, parent, false))
            }
            else -> {
                ViewHolderRepo(LayoutInflater.from(activity).inflate(R.layout.repo_item, parent, false))
            }
        }
    }

    override fun getItemCount(): Int = apps.size

    override fun getItemViewType(position: Int): Int {
        return if (apps[position].divider) 0 else 1
    }

    @SuppressLint("SetTextI18n", "RtlHardcoded")
    override fun onBindViewHolder(holder: RepoAdapter.ViewHolder, position: Int) {
        val application = apps[position]
        val darkMode = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_mode", true)

        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        val width = size.x

        if (holder !is ViewHolderRepo) {
            holder.title.text = application.name.toUpperCase()
        } else {
            holder.title.text = application.name
            holder.version.text = "v${application.version}"
            holder.update.text = "${activity.getString(R.string.update_on)}: ${application.latestUpdate}"
            holder.description.text = application.descriptionShort
            holder.dev.text = "${activity.getString(R.string.created_by)} ${application.author}"
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
                                "${activity.getString(R.string.download)} ${application.name}"
                            )
                        Network.downloadFile(
                            application.latestApk,
                            path,
                            "${application.name}-${application.version}.apk",
                            activity,
                            { _, bytes ->
                                progressDialog.setMessage(
                                    "${activity.getString(R.string.download)} ${application.name} (${
                                    /*if (progress > 0) "$progress%"
                                    else*/ Network.humanReadableByteCount(bytes, true)
                                    })"
                                )
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
                                fragment.loadData { }
                            })
                        dialog.dismiss()
                    }
                    .setNeutralButton(R.string.download) { dialog: DialogInterface, _: Int ->
                        val path = File(
                            File(Environment.getExternalStorageDirectory(), "ApplicationManager"),
                            application.name
                        )
                        if (!path.exists()) path.mkdirs()
                        val progressDialog =
                            ProgressDialog.show(
                                activity,
                                "",
                                "${activity.getString(R.string.download)} ${application.name}"
                            )
                        Network.downloadFile(
                            application.zipUrl,
                            path,
                            "${application.version}.zip",
                            activity,
                            { _, bytes ->
                                progressDialog.setMessage(
                                    "${activity.getString(R.string.download)} ${application.name} (${
                                    /*if (progress > 0) "$progress%"
                                    else*/ Network.humanReadableByteCount(bytes, true)
                                    })"
                                )
                            },
                            { _, b ->
                                progressDialog.dismiss()
                                if (!b)
                                    Toast.makeText(
                                        activity,
                                        activity.getString(R.string.downloaded),
                                        Toast.LENGTH_LONG
                                    ).show()
                                else
                                    Toast.makeText(
                                        activity,
                                        activity.getString(R.string.error_apk),
                                        Toast.LENGTH_LONG
                                    ).show()
                            })
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
                text.setPadding(10.dp(activity))
                text.setLineSpacing(0F, 1.2F)
                Thread {
                    val desc = application.loadDescription()
                    Logger.log(Logger.Companion.Type.INFO, "MARKDOWN", desc)
                    activity.runOnUiThread {
                        Markwon.setMarkdown(text, config, desc)
                    }
                }.start()
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
    }

    open class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.txt_title)
    }

    class ViewHolderRepo(v: View) : ViewHolder(v) {
        val update: TextView = v.findViewById(R.id.txt_update)
        val description: TextView = v.findViewById(R.id.txt_description)
        val download: ImageView = v.findViewById(R.id.download)
        val layout: ViewGroup = v.findViewById(R.id.layout)
        val dev: TextView = v.findViewById(R.id.txt_dev)
        val version: TextView = v.findViewById(R.id.txt_version)
    }
}