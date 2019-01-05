/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.core.content.edit
import com.dertyp7214.applicationmanager.App
import org.json.JSONObject

class Logs {
    companion object {
        fun log(type: Type, tag: String, body: Any?) {
            when (type) {
                Type.DEBUG -> Log.d(tag, body.toString())
                Type.ERROR -> Log.e(tag, body.toString())
                Type.INFO -> Log.i(tag, body.toString())
            }
            val context = App.context
            try {
                val sharedPreferences = context.getSharedPreferences("logs", MODE_PRIVATE)
                sharedPreferences.edit {
                    putString(
                        System.currentTimeMillis().toString(),
                        JSONObject("{\"type\": \"${type.name}\", \"body\": \"${body.toString()}\"}").toString()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        enum class Type {
            DEBUG,
            ERROR,
            INFO
        }
    }
}