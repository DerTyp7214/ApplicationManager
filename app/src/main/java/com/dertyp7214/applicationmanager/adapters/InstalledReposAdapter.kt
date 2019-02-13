/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.core.runAsCommand
import com.dertyp7214.applicationmanager.fragments.Installed
import com.dertyp7214.applicationmanager.helpers.RootChecker
import com.dertyp7214.applicationmanager.props.Application

class InstalledReposAdapter(
    private val fragment: Installed,
    private val recyclerView: RecyclerView,
    private val list: List<Application>
) : RecyclerView.Adapter<InstalledReposAdapter.ViewHolder>() {

    private val activity = fragment.activity!!
    private val applicationInfos = HashMap<String, ApplicationInfo>()
    private val darkMode: Boolean =
        PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("dark_mode", true)
    private val magiskInstalled = RootChecker.getInstance(activity.application).checkForMagisk().first

    init {
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = this
        list.forEach {
            applicationInfos[it.packageName] = activity.packageManager.getApplicationInfo(it.packageName, 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.installed_module, parent, false))
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val applicationInfo = applicationInfos[item.packageName]

        holder.title.text = item.name
        holder.version.text = activity.packageManager.getPackageInfo(item.packageName, 0).versionName
        holder.dev.text = "${activity.getString(R.string.created_by)} ${item.author}"
        holder.description.text = item.descriptionShort

        if (magiskInstalled) {
            holder.disable.visibility = VISIBLE
            holder.disable.isChecked = applicationInfo?.enabled ?: false
            holder.disable.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    "pm enable ${item.packageName}".runAsCommand()
                } else {
                    "pm disable ${item.packageName}".runAsCommand()
                }
            }
        } else {
            holder.disable.visibility = GONE
        }

        holder.delete.imageTintList = ColorStateList.valueOf(if (darkMode) Color.WHITE else Color.BLACK)
        holder.delete.setOnClickListener {
            activity.startActivityForResult(
                Intent(Intent.ACTION_DELETE, Uri.parse("package:${item.packageName}")),
                1337
            )
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.txt_title)
        val version: TextView = v.findViewById(R.id.txt_version)
        val dev: TextView = v.findViewById(R.id.txt_dev)
        val description: TextView = v.findViewById(R.id.txt_description_short)
        val disable: CheckBox = v.findViewById(R.id.check_box_disable)
        val delete: ImageView = v.findViewById(R.id.btn_delete)
    }
}