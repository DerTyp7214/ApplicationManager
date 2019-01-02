package com.dertyp7214.applicationmanager.github

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class Release(
    val id: Int,
    val node_id: String,
    val tag_name: String,
    val target_commitish: String,
    val name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val published_at: String,
    val asset: String,
    val body: String
) {
    companion object {
        val empty = Release(0, "", "", "", "", false, false, "", "", "")
    }

    @SuppressLint("SimpleDateFormat")
    fun published_at(): Date {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        return format.parse(published_at)
    }

    @SuppressLint("SimpleDateFormat")
    fun parsedPublishedAt(): String {
        return SimpleDateFormat("dd.MM.yyyy").format(published_at())
    }
}
