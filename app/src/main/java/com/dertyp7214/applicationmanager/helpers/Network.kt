/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class Network {
    companion object {
        var disabled = false
        fun getWebContent(url: String, startTime: Long = System.currentTimeMillis()): String {
            Logs.log(
                Logs.Companion.Type.DEBUG,
                "getWebContent",
                "url: $url, startTime: ${System.currentTimeMillis() - startTime}"
            )
            return try {
                val web = URL(url)
                val reader = BufferedReader(InputStreamReader(web.openStream()))

                val ret = StringBuilder()
                var line: String? = null

                while ({ line = reader.readLine(); line }() != null)
                    ret.append(line!!).append("\n")

                reader.close()
                ret.toString()
            } catch (e: ConnectException) {
                if (System.currentTimeMillis() - startTime > 300000)
                    return "{\"message\": \"Something went wrong.\"}"
                Thread.sleep(100)
                return getWebContent(url, startTime)
            } catch (e: Exception) {
                Logs.log(Logs.Companion.Type.ERROR, "getWebContent", e.message)
                ""
            }
        }

        fun getJSONObject(url: String, api_key: String = "", startTime: Long = System.currentTimeMillis()): String {
            Logs.log(
                Logs.Companion.Type.DEBUG,
                "getJSONObject",
                "url: $url, startTime: ${System.currentTimeMillis() - startTime}"
            )
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

                while ({ inputLine = reader.readLine(); inputLine }() != null)
                    ret.append(inputLine!!)

                reader.close()
                ret.toString()
            } catch (e: ConnectException) {
                if (System.currentTimeMillis() - startTime > 300000)
                    return "{\"message\": \"Something went wrong.\"}"
                Thread.sleep(100)
                return getJSONObject(url, api_key, startTime)
            } catch (e: SSLException) {
                if (System.currentTimeMillis() - startTime > 300000)
                    return "{\"message\": \"Something went wrong.\"}"
                Thread.sleep(100)
                return getJSONObject(url, api_key, startTime)
            } catch (e: UnknownHostException) {
                if (System.currentTimeMillis() - startTime > 300000)
                    return "{\"message\": \"Something went wrong.\"}"
                Thread.sleep(100)
                return getJSONObject(url, api_key, startTime)
            } catch (e: Exception) {
                e.printStackTrace()
                Logs.log(Logs.Companion.Type.ERROR, "getJSONObject", e.message)
                "{\"message\": \"Something went wrong.\"}"
            }
        }

        fun downloadFile(
            url: String,
            path: File,
            filename: String,
            onProgress: (progress: Int) -> Unit,
            onFinish: (file: File, error: Boolean) -> Unit
        ) {
            PRDownloader.download(url, path.absolutePath, filename)
                .build()
                .setOnProgressListener {
                    onProgress((it.currentBytes * 100 / it.totalBytes).toInt())
                }
                .setOnCancelListener {
                    onFinish(File(path, filename), true)
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        onFinish(File(path, filename), false)
                    }

                    override fun onError(error: Error?) {
                        Logs.log(
                            Logs.Companion.Type.ERROR,
                            "Network",
                            Pair(error?.isServerError, error?.isConnectionError)
                        )
                        onFinish(File(path, filename), true)
                    }
                })
        }
    }
}
