/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.github

import android.annotation.SuppressLint
import com.dertyp7214.applicationmanager.helpers.Logs
import com.dertyp7214.applicationmanager.props.RepoProp
import java.text.SimpleDateFormat
import java.util.*

class Repo(
    val repoProp: RepoProp,
    val id: Int,
    val node_id: String,
    val name: String,
    val full_name: String,
    val description: String,
    val url: String,
    val updated_at: String,
    val homepage: String
) {
    companion object {
        val empty = Repo(RepoProp(""), 0, "", "", "", "", "", "", "")
    }

    @SuppressLint("SimpleDateFormat")
    fun updated_at(): Date {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
            format.parse(updated_at)
        } catch (e: Exception) {
            Logs.log(Logs.Companion.Type.ERROR, "updated_at", e.message)
            Date()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun parsedUpdatedAt(): String {
        return SimpleDateFormat("dd.MM.yyyy").format(updated_at())
    }

    override fun toString(): String {
        return "repoProp: [$repoProp]\t" +
                "id: $id\t" +
                "node_id: $node_id\t" +
                "name: $name\t" +
                "full_name: $full_name\t" +
                "description: $description\t" +
                "url: $url\t" +
                "updated_at: $updated_at\t" +
                "updated_at_date: ${parsedUpdatedAt()}\t" +
                "homepage: $homepage"
    }
}
