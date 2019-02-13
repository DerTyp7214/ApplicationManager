package com.dertyp7214.applicationmanager.themes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dertyp7214.applicationmanager.App
import com.dertyp7214.applicationmanager.R
import com.dertyp7214.applicationmanager.screens.MainActivity
import kotlinx.android.synthetic.main.activity_theme_preview_screen.*

class ThemePreviewScreen : AppCompatActivity() {

    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_preview_screen)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val themes = arrayListOf(
            ThemePreview(
                "Theme1",
                ThemePreview.Theme(
                    R.color.colorPrimaryDark,
                    R.color.colorPrimary,
                    R.color.colorAccent,
                    R.style.AppTheme,
                    true
                )
            ),
            ThemePreview(
                "Theme2",
                ThemePreview.Theme(
                    R.color.colorPrimaryGreenDark,
                    R.color.colorPrimaryGreen,
                    R.color.colorAccentGreen,
                    R.style.AppTheme_Green,
                    true
                )
            ),
            ThemePreview(
                "Theme3",
                ThemePreview.Theme(
                    R.color.colorPrimaryRedDark,
                    R.color.colorPrimaryRed,
                    R.color.colorAccentRed,
                    R.style.AppTheme_Red,
                    true
                )
            )
        )

        adapter = ViewPagerAdapter(supportFragmentManager)
        themes.forEach {
            adapter.addFragment(it)
        }

        viewPager.adapter = adapter

        viewPager.currentItem = when ((application as App).getTheme(false, noActionBar = false)) {
            R.style.AppTheme_Green -> 1
            R.style.AppTheme_Red -> 2
            else -> 0
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
            }
        })

        button.setOnClickListener {
            App.setTheme(themes[viewPager.currentItem].theme.themeId)
            goBack()
        }
    }

    private fun goBack() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("fragment", R.id.nav_dev_settings)
        })
        finish()
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add("")
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }
}