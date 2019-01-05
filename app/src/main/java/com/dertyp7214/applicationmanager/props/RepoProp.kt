/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.props

class RepoProp(content: String) {

    var author = ""
        private set(value) {
            field = value
        }
    var id = ""
        private set(value) {
            field = value
        }
    var description = ""
        private set(value) {
            field = value
        }

    override fun toString(): String {
        return "author: $author\t" +
                "id: $id\t" +
                "description: $description"
    }

    init {
        content.split("\n").forEach {
            when {
                it.startsWith("author") -> author = it.replace("author=", "")
                it.startsWith("id") -> id = it.replace("id=", "")
                it.startsWith("description") -> description = it.replace("description=", "")
            }
        }
    }
}
