package za.co.woolworths.financial.services.android.ui.views

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ConfigureViewPagerWithTab(val activity: AppCompatActivity?, private val viewPager: ViewPager2?, private val tabLayout: TabLayout?, val listItem: Map<String, Fragment>?, private val defaultViewPagerPosition: Int = 0, private val adapterOnClick: (Int) -> Unit) {

    fun create() {

        viewPager?.visibility = View.VISIBLE
        viewPager?.offscreenPageLimit = listItem?.size ?: 0
        tabLayout?.visibility = View.VISIBLE

        activity?.let { activity ->
            val pagerAdapter = object : FragmentStateAdapter(activity) {
                override fun createFragment(position: Int): Fragment = listItem?.values?.elementAt(position) ?: Fragment()
                override fun getItemCount(): Int = listItem?.size ?: 0
            }

            viewPager?.adapter = pagerAdapter

            tabLayout?.let { tabLayout ->
                viewPager?.let { viewPager ->
                    TabLayoutMediator(tabLayout, viewPager) { tab, index ->
                        tab.text = listItem?.keys?.elementAt(index)
                    }.attach()
                }
            }


            tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    updateTabFont(tab?.position ?: 0, false)
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val tabPosition = tab?.position ?: 0
                    updateTabFont(tabPosition, true)
                    adapterOnClick.invoke(tabPosition)
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            viewPager?.currentItem = defaultViewPagerPosition
        }
    }

    private fun updateTabFont(position: Int, tabIsSelected: Boolean) {
        val viewGroup = tabLayout?.getChildAt(0) as? ViewGroup
        val tabPosition = viewGroup?.getChildAt(position) as? LinearLayout
        val tabView = tabPosition?.getChildAt(1) as? AppCompatTextView
        tabView?.setTypeface(activity?.let { activity -> ResourcesCompat.getFont(activity, if (tabIsSelected) R.font.futura_semi_bold_ttf else R.font.futura_medium_ttf) }, Typeface.NORMAL)
    }
}