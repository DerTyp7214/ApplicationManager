package com.dertyp7214.applicationmanager.themes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.dertyp7214.applicationmanager.App
import com.dertyp7214.applicationmanager.R
import kotlinx.android.synthetic.main.activity_theme_preview_screen.*

class ThemePreviewScreen : AppCompatActivity() {

    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_preview_screen)

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
                "Theme1",
                ThemePreview.Theme(
                    R.color.colorPrimaryGreenDark,
                    R.color.colorPrimaryGreen,
                    R.color.colorAccentGreen,
                    R.style.AppTheme_Green,
                    true
                )
            )
        )

        adapter = ViewPagerAdapter(supportFragmentManager)
        themes.forEach {
            adapter.addFragment(it)
        }

        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val theme = themes[position]
                App.setTheme(theme.theme.themeId)
            }
        })
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