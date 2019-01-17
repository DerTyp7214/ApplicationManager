/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.props

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
                "latestUpdate: $latestUpdate"
    }

    companion object {
        fun divider(text: String): Application {
            return Application(0, text, "", "", "", "", "", "", "", "", true)
        }
    }
}
