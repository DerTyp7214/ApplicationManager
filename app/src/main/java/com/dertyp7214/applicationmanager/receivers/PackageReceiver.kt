package com.dertyp7214.applicationmanager.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dertyp7214.applicationmanager.fragments.Installed
import com.dertyp7214.applicationmanager.fragments.Repos

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_PACKAGE_ADDED || intent.action == Intent.ACTION_PACKAGE_REMOVED) {
            Installed.instance?.loadInstalledModules()
            Repos.instance?.loadData { }
        }
    }
}