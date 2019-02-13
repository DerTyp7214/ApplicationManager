package com.dertyp7214.applicationmanager.core

import org.json.JSONArray

fun JSONArray.find(text: String): Int {
    var index = -1
    forEach { s, i ->
        if (s == text) index = i
    }
    return index
}

fun JSONArray.forEach(run: (text: String, index: Int) -> Unit) {
    for (i in 0 until length())
        run(getString(i), i)
}

fun JSONArray.contains(text: String): Boolean {
    var contains = false
    forEach { s, _ ->
        if (s == text) contains = true
    }
    return contains
}