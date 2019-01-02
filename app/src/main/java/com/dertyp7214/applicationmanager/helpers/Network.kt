package com.dertyp7214.applicationmanager.helpers

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Network {
    companion object {
        fun getWebContent(url: String): String {
            return try {
                val web = URL(url)
                val reader = BufferedReader(InputStreamReader(web.openStream()))

                val ret = StringBuilder()
                var line: String? = null

                while ({ line = reader.readLine(); line }() != null)
                    ret.append(line!!).append("\n")

                if (url == "http://api.github.com/repos/DerTyp7214/ApkMirror/releases/latest") Log.d(
                    "RET",
                    ret.toString()
                )
                reader.close()
                ret.toString()
            } catch (e: Exception) {
                Log.d("ERROR", e.message)
                ""
            }
        }

        fun getJSONObject(url: String, api_key: String = ""): String {
            return try {
                val web = URL(url)
                val connection = web.openConnection() as HttpURLConnection
                connection.setRequestProperty("Authorization", "token $api_key")
                val reader: BufferedReader

                reader = if (api_key.isEmpty())
                    BufferedReader(InputStreamReader(web.openStream()))
                else
                    BufferedReader(InputStreamReader(connection.inputStream))

                var inputLine: String? = null
                val ret = StringBuilder()

                while ({inputLine = reader.readLine(); inputLine}() != null)
                    ret.append(inputLine)

                reader.close()
                ret.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                "{\"message\": \"Something went wrong.\"}"
            }
        }
    }
}
