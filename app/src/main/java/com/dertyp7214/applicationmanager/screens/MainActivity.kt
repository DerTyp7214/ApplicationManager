/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.fragments.Home
import com.dertyp7214.applicationmanager.fragments.Repos
import com.dertyp7214.logs.fragments.Logs
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var toolbarElevation: Float = 0F
    private var currentFragment: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        toolbarElevation = toolbar.elevation

        val path = File(Environment.getExternalStorageDirectory(), ".application_manager")
        path.deleteRecursively()
        path.mkdirs()

        setFragment(Home(this), R.id.nav_home)
        title = getString(R.string.home)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toolbar.menu.clear()
        toolbar.elevation = toolbarElevation
        when (item.itemId) {
            R.id.nav_home -> {
                title = getString(R.string.home)
                setFragment(Home(this), R.id.nav_home)
            }
            R.id.nav_installed -> {
                title = getString(R.string.installed)
            }
            R.id.nav_repos -> {
                title = getString(R.string.repos)
                setFragment(Repos(this), R.id.nav_repos)
            }
            R.id.nav_log -> {
                title = getString(R.string.logs)
                toolbar.elevation = 0F
                val itemDelete =
                    toolbar.menu.add(0, R.id.menu_delete, Menu.NONE, getString(R.string.menu_action_delete))
                itemDelete.setIcon(R.drawable.ic_action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                itemDelete.setOnMenuItemClickListener {
                    getSharedPreferences("logs", Context.MODE_PRIVATE).edit {
                        clear()
                    }
                    setFragment(Logs(), R.id.nav_log, false, true)
                    true
                }
                setFragment(Logs(), R.id.nav_log)
            }
            R.id.nav_settings -> {
                title = getString(R.string.settings)
                val i = 55 / 0
            }
            R.id.nav_about -> {
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun startScreen(clazz: Class<*>) {
        Handler().postDelayed({
            startActivity(Intent(this, clazz))
        }, 250)
    }

    private fun setFragment(fragment: Fragment, id: Int, animated: Boolean = true, forceReplace: Boolean = false) {
        if (currentFragment != id || forceReplace)
            Handler().postDelayed({
                currentFragment = id
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.frameLayout, fragment)
                if (animated) fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                fragmentTransaction.commit()
            }, 250)
    }
}
