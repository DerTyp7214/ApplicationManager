/*
 * Copyright (c) 2019.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.applicationmanager.screens

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.fragments.Home
import com.dertyp7214.applicationmanager.fragments.Repos
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


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

        setFragment(Home(this))
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
        when (item.itemId) {
            R.id.nav_home -> {
                title = getString(R.string.home)
                setFragment(Home(this))
            }
            R.id.nav_installed -> {
                title = getString(R.string.installed)
            }
            R.id.nav_repos -> {
                title = getString(R.string.repos)
                setFragment(Repos(this))
            }
            R.id.nav_log -> {
                startScreen(Logs::class.java)
            }
            R.id.nav_settings -> {
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

    private fun setFragment(fragment: Fragment) {
        Handler().postDelayed({
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frameLayout, fragment)
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            fragmentTransaction.commit()
        }, 250)
    }
}
