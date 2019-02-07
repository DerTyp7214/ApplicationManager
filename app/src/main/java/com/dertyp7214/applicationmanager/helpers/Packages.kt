/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.dertyp7214.logs.helpers.Logger
import com.dertyp7214.preferencesplus.core.nextActivity
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
                            context.nextActivity()!!.startActivityForResult(intent, 1337)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(
                                Uri.fromFile(file),
                                "application/vnd.android.package-archive"
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.nextActivity()!!.startActivityForResult(intent, 1337)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Logger.log(Logger.Companion.Type.ERROR, "install", e.message)
            }
        }

        private fun getFileUri(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(
                context,
                context.applicationContext
                    .packageName + ".GenericFileProvider", file
            )
        }

        fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun getVersion(packageName: String, packageManager: PackageManager): String {
            val packageInfo: PackageInfo? = packageManager.getPackageInfo(packageName, 0)
            return packageInfo?.versionName ?: "1.0"
        }
    }
}