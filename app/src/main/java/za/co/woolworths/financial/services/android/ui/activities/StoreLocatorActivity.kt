package za.co.woolworths.financial.services.android.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.store_locator_activity.*
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorListFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorViewModel
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils

class StoreLocatorActivity : AppCompatActivity() {

    lateinit var storeLocatorViewModel: StoreLocatorViewModel

    companion object {
        private const val UNSELECTED_TAB_ALPHA_VIEW = 0.3f
        private const val SELECTED_TAB_ALPHA_VIEW = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this, android.R.color.transparent)
        setContentView(R.layout.store_locator_activity)
        storeLocatorViewModel = ViewModelProviders.of(this)[StoreLocatorViewModel::class.java]
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), StoreLocatorFragment.REQUEST_LOCATION_PERMISSION)
        initViewPagerWithTabLayout()
    }

    private fun initViewPagerWithTabLayout() {
        vpStoreLocator?.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> StoreLocatorFragment.newInstance()
                    else -> StoreLocatorListFragment.newInstance()
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        TabLayoutMediator(tabs, vpStoreLocator) { _, _ -> }.attach()

        val tabMapLayout = tabs?.getTabAt(0)
        val tabListLayout = tabs?.getTabAt(1)

        tabMapLayout?.setCustomView(R.layout.stockfinder_custom_tab)
        tabListLayout?.setCustomView(R.layout.stockfinder_custom_tab)

        val mapView = tabMapLayout?.customView
        val listView = tabListLayout?.customView

        val imMapView = mapView?.findViewById<ImageView>(R.id.tabIcon)
        val tvMapView = mapView?.findViewById<WTextView>(R.id.textIcon)
        val imListView = listView?.findViewById<ImageView>(R.id.tabIcon)
        val tvListView = listView?.findViewById<WTextView>(R.id.textIcon)

        tvMapView?.text = getString(R.string.stock_finder_map_view)
        tvListView?.text = getString(R.string.stock_finder_list_view)

        imMapView?.setImageResource(R.drawable.mapview)
        imListView?.setImageResource(R.drawable.listview)

        onTabSelected(0, tvMapView, imMapView, tvListView, imListView)

        vpStoreLocator?.currentItem = 0

        // Disable ViewPager swipe
        vpStoreLocator?.isUserInputEnabled = false

        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) = onTabSelected(tab?.position
                    ?: 0, tvMapView, imMapView, tvListView, imListView)


            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun onTabSelected(position: Int, tvMapView: WTextView?, imMapView: ImageView?, tvListView: WTextView?, imListView: ImageView?) {
        when (position) {
            0 -> {
                tvMapView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = SELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
            }
            1 -> {
                tvMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = SELECTED_TAB_ALPHA_VIEW
            }
            else -> return
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            StoreLocatorFragment.REQUEST_LOCATION_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storeLocatorViewModel.requestLocationUpdate()
            }
            else -> return
        }
    }
}