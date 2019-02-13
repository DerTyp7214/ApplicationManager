/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.content.Context
import android.preference.PreferenceManager
import com.dertyp7214.applicationmanager.github.Release
import com.dertyp7214.applicationmanager.github.Repo
import com.dertyp7214.applicationmanager.helpers.Config.API_KEY
import com.dertyp7214.applicationmanager.helpers.Network.Companion.getJSONObject
import com.dertyp7214.applicationmanager.helpers.Network.Companion.getWebContent
import com.dertyp7214.applicationmanager.props.Application
import com.dertyp7214.applicationmanager.props.RepoProp
import com.dertyp7214.applicationmanager.thirdparty.Repo3
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class Api(private val context: Context) {
    companion object {
        const val baseUrl = "https://api.github.com"
        const val github = "https://github.com"
        const val username = "Application-Repo"
    }

    private fun JSONArray.forEach(unit: (Any, Int) -> Unit) {
        for (i in 0 until length()) {
            unit(this[i], i)
        }
    }

    private class SafeJSONArray(s: String) : JSONArray(s) {
        constructor(obj: JSONArray) : this(obj.toString())

        override fun getJSONObject(index: Int): JSONObject {
            return if (length() > 0) super.getJSONObject(index)
            else JSONObject("{}")
        }
    }

    private class SafeJSONObject(s: String) : JSONObject(s) {
        constructor(obj: JSONObject) : this(obj.toString())

        override fun getString(name: String?): String {
            return if (name != null) try {
                if (super.getString(name) != "null") super.getString(name)
                else ""
            } catch (e: Exception) {
                ""
            } else ""
        }

        override fun getInt(name: String?): Int {
            return if (name != null) try {
                super.getInt(name)
            } catch (e: Exception) {
                0
            } else 0
        }

        override fun getBoolean(name: String?): Boolean {
            return if (name != null) try {
                super.getBoolean(name)
            } catch (e: Exception) {
                false
            } else false
        }

        override fun getJSONArray(name: String?): JSONArray {
            return if (name != null) try {
                super.getJSONArray(name)
            } catch (e: Exception) {
                JSONArray("[{}]")
            } else JSONArray("[{}]")
        }
    }

    private class ReadJSON(val url: String) {
        fun readObject(): Pair<SafeJSONObject, Boolean> {
            return try {
                Pair(SafeJSONObject(getJSONObject(url, API_KEY)), true)
            } catch (e: Exception) {
                Pair(SafeJSONObject("{}"), false)
            }
        }

        fun readArray(): Pair<JSONArray, Boolean> {
            return try {
                Pair(JSONArray(getJSONObject(url, API_KEY)), true)
            } catch (e: Exception) {
                Pair(JSONArray("[]"), false)
            }
        }
    }

    fun getRepos(query: String = ""): List<Repo> {
        val url = "$baseUrl/users/$username/repos"
        val readJson = ReadJSON(url).readArray()
        return if (readJson.second) {
            val list = ArrayList<Repo>()
            readJson.first.forEach { any, i ->
                val obj = any as JSONObject
                val repo = parseRepo(obj)
                if (repo.name != "application-repo.github.io" && (repo.name.contains(
                        query,
                        true
                    ) || query.isEmpty())
                ) {
                    list.add(repo)
                }
            }
            list.sortedWith(kotlin.Comparator { o1, o2 ->
                o1.name.compareTo(o2.name, true)
            })
        } else {
            Arrays.asList(Repo.empty)
        }
    }

    fun getThirdPartyRepos(query: String = ""): List<Repo3> {
        val repos = ArrayList<Repo3>()
        PreferenceManager.getDefaultSharedPreferences(context).getStringSet("repos", mutableSetOf())!!.forEach {
            val readJson = ReadJSON("$baseUrl/users/$it/repos").readArray()
            if (readJson.second) {
                readJson.first.forEach { any, _ ->
                    val repo = parseRepo3(any as JSONObject, it)
                    if (repo.name.contains(query, true) || repo.name.isEmpty())
                        repos.add(repo)
                }
            }
        }
        return repos
    }

    fun loadApplications(repos: List<Repo>): List<Application> {
        return repos.map {
            val renderer = HtmlRenderer.builder().build()
            val parser = Parser.builder().build()
            val release = releases(it.name, true).first()
            val id = it.id
            val name = it.name
            val version = release.tag_name
            val author = it.repoProp.author
            val packageName = it.repoProp.id
            val description =
                "https://raw.githubusercontent.com/$username/${it.name}/${if (it.description.isEmpty()) "master" else it.description}/README.md"
            val descriptionShort = it.repoProp.description
            val latestChanges = renderer.render(parser.parse(release.body))
            val latestApk = release.asset
            val latestUpdate = it.parsedUpdatedAt()
            val zipUrl =
                "$github/$username/$name/zipball/${if (it.description.isEmpty()) "master" else it.description}"
            Application(
                id,
                name,
                version,
                author,
                packageName,
                description,
                descriptionShort,
                latestChanges,
                latestApk,
                latestUpdate,
                zipUrl
            )
        }
    }

    fun releases(repo: String, latest: Boolean = false, host: String = baseUrl): List<Release> {
        val pair = if (latest)
            ReadJSON("$host/repos/$username/$repo/releases/latest").readObject()
        else
            ReadJSON("$host/repos/$username/$repo/releases").readArray()
        return if (pair.second) {
            val releases = ArrayList<Release>()
            val first = pair.first
            if (first is SafeJSONObject) {
                releases.add(parseRelease(first))
            } else if (first is JSONArray) {
                first.forEach { any, i ->
                    releases.add(parseRelease(any as JSONObject))
                }
            }
            releases
        } else {
            Arrays.asList(Release.empty)
        }
    }

    private fun parseRelease(jsonObject: JSONObject): Release {
        val obj = SafeJSONObject(jsonObject)
        val id = obj.getInt("id")
        val nodeId = obj.getString("node_id")
        val tagName = obj.getString("tag_name")
        val targetCommitish = obj.getString("target_commitish")
        val name = obj.getString("name")
        val draft = obj.getBoolean("draft")
        val prerelease = obj.getBoolean("prerelease")
        val publishedAt = obj.getString("published_at")
        val asset =
            SafeJSONObject(
                SafeJSONArray(
                    obj.getJSONArray("assets")
                ).getJSONObject(0)
            ).getString("browser_download_url")
        val body = obj.getString("body")
        return Release(
            id,
            nodeId,
            tagName,
            targetCommitish,
            name,
            draft,
            prerelease,
            publishedAt,
            asset,
            body
        )
    }

    private fun parseRepo(obj: JSONObject): Repo {
        val safeObj = SafeJSONObject(obj)
        val id = safeObj.getInt("id")
        val nodeId = safeObj.getString("node_id")
        val name = safeObj.getString("name")
        val fullName = safeObj.getString("full_name")
        val description = safeObj.getString("description")
        val url = safeObj.getString("url")
        val updatedAt = safeObj.getString("updated_at")
        val homepage = safeObj.getString("homepage")
        val repoProp =
            RepoProp(getWebContent("https://raw.githubusercontent.com/$username/$name/${if (description.isEmpty()) "master" else description}/repo.prop"))
        return Repo(repoProp, id, nodeId, name, fullName, description, url, updatedAt, homepage)
    }

    private fun parseRepo3(obj: JSONObject, host: String): Repo3 {
        val safeObj = SafeJSONObject(obj)
        val id = safeObj.getInt("id")
        val nodeId = safeObj.getString("node_id")
        val name = safeObj.getString("name")
        val fullName = safeObj.getString("full_name")
        val description = safeObj.getString("description")
        val url = safeObj.getString("url")
        val updatedAt = safeObj.getString("updated_at")
        val homepage = safeObj.getString("homepage")
        val repoProp =
            RepoProp(getWebContent("https://raw.githubusercontent.com/$host/$name/${if (description.isEmpty()) "master" else description}/repo.prop"))
        return Repo3(repoProp, id, nodeId, name, fullName, description, url, updatedAt, homepage, host)
    }
}
