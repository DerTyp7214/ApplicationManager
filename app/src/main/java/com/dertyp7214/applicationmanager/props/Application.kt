/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.props

import com.dertyp7214.applicationmanager.helpers.Network
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.json.JSONObject

class Application(
    val id: Int,
    val name: String,
    val version: String,
    val author: String,
    val packageName: String,
    val description: String,
    val descriptionShort: String,
    val latestChanges: String,
    val latestApk: String,
    val latestUpdate: String,
    val zipUrl: String,
    val divider: Boolean = false
) {
    override fun toString(): String {
        return "id: $id\t" +
                "name: $name\t" +
                "version: $version\t" +
                "author: $author\t" +
                "packageName: $packageName\t" +
                "description: $description\t" +
                "descriptionShort: $descriptionShort\t" +
                "latestChanges: $latestChanges\t" +
                "latestApk: $latestApk\t" +
                "latestUpdate: $latestUpdate\n" +
                "zipUrl: $zipUrl"
    }

    fun loadDescription(): String {
        return if (description.startsWith("http")) {
            val renderer = HtmlRenderer.builder().build()
            val parser = Parser.builder().build()
            renderer.render(parser.parse(Network.getWebContent(description)))
        } else description
    }

    fun getData(): JSONObject {
        val obj = JSONObject()
        obj.put("id", id)
        obj.put("name", name)
        obj.put("version", version)
        obj.put("author", author)
        obj.put("packageName", packageName)
        obj.put("description", description)
        obj.put("descriptionShort", descriptionShort)
        obj.put("latestChanges", latestChanges)
        obj.put("latestApk", latestApk)
        obj.put("latestUpdate", latestUpdate)
        obj.put("zipUrl", zipUrl)
        obj.put("divider", divider)
        return obj
    }

    companion object {
        fun divider(text: String): Application {
            return Application(0, text, "", "", "", "", "", "", "", "", "", true)
        }

        fun setData(obj: JSONObject): Application {
            return Application(
                obj.getInt("id"),
                obj.getString("name"),
                obj.getString("version"),
                obj.getString("author"),
                obj.getString("packageName"),
                obj.getString("description"),
                obj.getString("descriptionShort"),
                obj.getString("latestChanges"),
                obj.getString("latestApk"),
                obj.getString("latestUpdate"),
                obj.getString("zipUrl"),
                obj.getBoolean("divider")
            )
        }
    }
}
