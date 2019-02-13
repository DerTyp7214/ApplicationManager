/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.dertyp7214.applicationmanager.core.loadApplications
import com.dertyp7214.applicationmanager.props.Application
import org.json.JSONArray

class RepoLoader private constructor(private val context: Context) {

    private var api: Api = Api(context)

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: RepoLoader? = null

        fun getInstance(context: Context): RepoLoader {
            if (instance == null)
                instance = RepoLoader(context)
            return instance!!
        }
    }

    fun loadRepo() {
        context.getSharedPreferences("repo", MODE_PRIVATE).edit {
            putBoolean("first", false)
        }
        loading = true
        val list = ArrayList(api.loadApplications(api.getRepos("")))
        list.addAll(api.getThirdPartyRepos("").loadApplications(context))
        saveList("repo", JsonParser.applicationToJSON(list))
        loading = false
    }

    fun loadRepoAsync() {
        Thread {
            loadRepo()
        }.start()
    }

    fun getRepoList(query: String = ""): List<Application> {
        return JsonParser.jsonToApplication(loadList("repo")).filter {
            query.isBlank() || it.name.contains(query, true) || it.author.contains(query, true)
        }
    }

    fun saveList(key: String, array: JSONArray) {
        context.getSharedPreferences("repoLoader", MODE_PRIVATE).edit {
            putString(key, array.toString())
        }
    }

    fun loadList(key: String): JSONArray =
        JSONArray(context.getSharedPreferences("repoLoader", MODE_PRIVATE).getString(key, "[]"))

    var loading: Boolean = false
        private set(value) {
            field = value
        }
    var error: Boolean = false
        private set(value) {
            field = value
        }
    var first: Boolean = true
        get() {
            return context.getSharedPreferences("repo", MODE_PRIVATE).getBoolean("first", true)
        }
        private set(value) {
            field = value
        }
}