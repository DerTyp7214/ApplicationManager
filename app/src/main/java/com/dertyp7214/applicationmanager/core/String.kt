package com.dertyp7214.applicationmanager.core

import com.dertyp7214.logs.helpers.Logger
import java.io.DataOutputStream

fun String.runAsCommand(): Boolean {
    return try {
        Logger.log(Logger.Companion.Type.DEBUG, "executeCommand", this)
        val process = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(process.outputStream)

        os.writeBytes(this + "\n")

        os.writeBytes("exit\n")
        os.flush()
        os.close()

        process.waitFor()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}