/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.util.regex.Pattern

class Packages {
    companion object {
        fun install(context: Context, file: File) {
            try {
                if (file.exists()) {
                    val fileNameArray =
                        file.name.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (fileNameArray[fileNameArray.size - 1] == "apk") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val downloadedApk = getFileUri(context, file)
                            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(
                                downloadedApk,
                                "application/vnd.android.package-archive"
                            )
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(
                                Uri.fromFile(file),
                                "application/vnd.android.package-archive"
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Logs.log(Logs.Companion.Type.ERROR, "install", e.message)
            }
        }

        private fun getFileUri(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(
                context,
                context.applicationContext
                    .packageName + ".GenericFileProvider", file
            )
        }
    }
}