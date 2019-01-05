/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.applicationmanager.BuildConfig
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.helpers.Logs
import com.dertyp7214.applicationmanager.helpers.Ui
import kotlinx.android.synthetic.main.activity_logs.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Logs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.dertyp7214.applicationmanager.R.layout.activity_logs)

        val sharedPreferences = getSharedPreferences("logs", Context.MODE_PRIVATE)

        val logs = ArrayList<Pair<String, Pair<String, String>>>()

        sharedPreferences.all.keys.forEach {
            val key = it
            val obj = JSONObject(sharedPreferences.getString(key, ""))
            val type = obj.getString("type")
            val body = obj.getString("body")

            if (stringToType(type) == Logs.Companion.Type.DEBUG && BuildConfig.DEBUG)
                logs.add(Pair(key, Pair(body, type)))
            else if (stringToType(type) != Logs.Companion.Type.DEBUG)
                logs.add(Pair(key, Pair(body, type)))
        }

        rv.adapter = LogsAdapter(this, logs.sortedWith(kotlin.Comparator { o1, o2 ->
            o1.first.toLong().compareTo(o2.first.toLong())
        }).reversed())
        val layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(
            rv.context,
            layoutManager.orientation
        )
        rv.layoutManager = layoutManager
        rv.addItemDecoration(dividerItemDecoration)

        title = getString(R.string.logs)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun stringToType(type: String): Logs.Companion.Type {
        return when (type) {
            Logs.Companion.Type.ERROR.name -> Logs.Companion.Type.ERROR
            Logs.Companion.Type.DEBUG.name -> Logs.Companion.Type.DEBUG
            else -> Logs.Companion.Type.INFO
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logs, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_reset -> {
                getSharedPreferences("logs", Context.MODE_PRIVATE).edit {
                    clear()
                }
                recreate()
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }

        return true
    }

    class LogsAdapter(val activity: Activity, val logs: List<Pair<String, Pair<String, String>>>) :
        RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(activity).inflate(R.layout.log_item, parent, false)

            return ViewHolder(v)
        }

        override fun getItemCount(): Int = logs.size

        @SuppressLint("SimpleDateFormat")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pair = logs[position]
            val title = SimpleDateFormat("dd/MM/yy hh:mm:ss").format(Date(pair.first.toLong()))

            holder.time.text = title
            holder.body.text = pair.second.first
            holder.type.text = pair.second.second
            if (pair.second.second == Logs.Companion.Type.ERROR.name)
                holder.type.setTextColor(activity.resources.getColor(android.R.color.holo_red_light))

            holder.layout.setOnClickListener {
                val size = Point()
                activity.windowManager.defaultDisplay.getSize(size)
                val width = size.x
                val dialog = AlertDialog.Builder(activity)
                    .setPositiveButton(activity.getString(android.R.string.ok)) { dialog, _ -> dialog.dismiss() }
                    .setTitle(title)
                    .setMessage(pair.second.first)
                    .create()
                dialog.show()
                dialog.window?.decorView?.layoutParams?.width = (width * 0.85F).toInt()
                dialog.window
                    ?.setBackgroundDrawable(ColorDrawable(Ui.getAttrColor(activity, android.R.attr.windowBackground)))
            }
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val time: TextView = v.findViewById(R.id.txt_time)
            val type: TextView = v.findViewById(R.id.txt_type)
            val body: TextView = v.findViewById(R.id.txt_body)
            val layout: ViewGroup = v.findViewById(R.id.layout)
        }
    }
}
