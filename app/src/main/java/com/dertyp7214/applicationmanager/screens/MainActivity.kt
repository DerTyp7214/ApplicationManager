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
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionManager
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.fragments.*
import com.dertyp7214.applicationmanager.helpers.RepoLoader
import com.dertyp7214.logs.fragments.Logs
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var navView: NavigationView
    private var toolbarElevation: Float = 0F
    private var currentFragment: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        Thread {
            val loader = RepoLoader.getInstance(this@MainActivity)
            val first = loader.first
            loader.loadRepo()
            if (first && currentFragment == R.id.nav_repos) Repos.triggerAsync("")
        }.start()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        val devSettingsItem = navView.menu.findItem(R.id.nav_dev_settings)
        if (false && !getSharedPreferences("dev_settings", MODE_PRIVATE).getBoolean(
                "enabled",
                false
            )
        ) // TODO: delete false
            devSettingsItem.isVisible = false

        toolbarElevation = toolbar.elevation

        val path = File(Environment.getExternalStorageDirectory(), ".application_manager")
        path.deleteRecursively()
        path.mkdirs()

        setFragment(Home(), R.id.nav_home)
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            currentFragment != R.id.nav_home -> {
                setFragment(Home(), R.id.nav_home)
                navView.setCheckedItem(R.id.nav_home)
            }
            else -> super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        toolbar.menu.clear()
        toolbar.elevation = toolbarElevation
        when (item.itemId) {
            R.id.nav_home -> {
                setFragment(Home(), R.id.nav_home)
            }
            R.id.nav_installed -> {
                title = getString(R.string.installed)
            }
            R.id.nav_repos -> {
                setFragment(Repos(), R.id.nav_repos)
                toolbar.inflateMenu(R.menu.search)
                val search = toolbar.menu.findItem(R.id.menu_search).actionView as SearchView
                search.apply {
                    setOnSearchClickListener {
                        TransitionManager.beginDelayedTransition(toolbar)
                        toolbar.menu.findItem(R.id.menu_search).expandActionView()
                    }
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            if (currentFragment == R.id.nav_repos)
                                Repos.triggerAsync(query ?: "")
                            val view = this@MainActivity.currentFocus
                            if (view != null) {
                                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(view.windowToken, 0)
                            }
                            return true
                        }

                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (currentFragment == R.id.nav_repos)
                                Repos.triggerAsync(newText ?: "")
                            return true
                        }
                    })
                }
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
                setFragment(Settings(), R.id.nav_settings)
            }
            R.id.nav_dev_settings -> {
                setFragment(DevSettings(), R.id.nav_dev_settings)
            }
            R.id.nav_about -> {
                setFragment(About(), R.id.nav_about)
            }
            R.id.nav_crash-> {
                val i = 55 / 0
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
