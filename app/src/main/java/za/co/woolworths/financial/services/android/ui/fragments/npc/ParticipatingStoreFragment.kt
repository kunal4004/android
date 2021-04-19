package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.participating_store_fragment.*
import kotlinx.android.synthetic.main.store_locator_activity.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorListFragment
import za.co.woolworths.financial.services.android.ui.views.WTextView

class ParticipatingStoreFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.participating_store_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayoutAndViewPager()
    }

    private fun setupTabLayoutAndViewPager() {
        selectStoreViewPager2?.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> StoreLocatorFragment.newInstance()
                    else -> StoreLocatorListFragment.newInstance()
                }
            }

            override fun getItemCount(): Int = 2
        }

        TabLayoutMediator(participatingStoreTabLayout, vpStoreLocator) { _, _ -> }.attach()

        val participatingStoreMapTab = participatingStoreTabLayout?.getTabAt(0)
        val participatingStoreListTab = participatingStoreTabLayout?.getTabAt(1)

        participatingStoreMapTab?.setCustomView(R.layout.stockfinder_custom_tab)
        participatingStoreListTab?.setCustomView(R.layout.stockfinder_custom_tab)

        val mapView = participatingStoreMapTab?.customView
        val listView = participatingStoreListTab?.customView

        val imMapView = mapView?.findViewById<ImageView>(R.id.tabIcon)
        val tvMapView = mapView?.findViewById<WTextView>(R.id.textIcon)
        val imListView = listView?.findViewById<ImageView>(R.id.tabIcon)
        val tvListView = listView?.findViewById<WTextView>(R.id.textIcon)

        tvMapView?.text = getString(R.string.stock_finder_map_view)
        tvListView?.text = getString(R.string.stock_finder_list_view)

        imMapView?.setImageResource(R.drawable.mapview)
        imListView?.setImageResource(R.drawable.listview)

        onTabSelected(0, tvMapView, imMapView, tvListView, imListView)

        selectStoreViewPager2?.currentItem = 0

        // Disable ViewPager swipe
        selectStoreViewPager2?.isUserInputEnabled = false

        tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) = onTabSelected(tab?.position
                    ?: 0, tvMapView, imMapView, tvListView, imListView)

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun onTabSelected(position: Int, tvMapView: WTextView?, imMapView: ImageView?, tvListView: WTextView?, imListView: ImageView?) {
        when (position) {
            0 -> {
                tvMapView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = SELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                tvTitle?.text = bindString(R.string.participating_stores)
            }
            1 -> {
                tvMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                tvTitle?.text = bindString(R.string.nearest_store)
            }
            else -> return
        }
    }

    companion object {
        private const val UNSELECTED_TAB_ALPHA_VIEW = 0.3f
        private const val SELECTED_TAB_ALPHA_VIEW = 1.0f
    }
}