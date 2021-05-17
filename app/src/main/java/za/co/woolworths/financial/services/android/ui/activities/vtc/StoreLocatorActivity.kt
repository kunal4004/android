package za.co.woolworths.financial.services.android.ui.activities.vtc

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.store_locator_activity.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorListFragment
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils

class StoreLocatorActivity : AppCompatActivity() {

    private var mTitle: String? = null
    private var mDescription: String? = null
    private var mLocations: MutableList<StoreDetails>? = null

    companion object {
        const val PRODUCT_NAME = "PRODUCT_NAME"
        const val CONTACT_INFO = "CONTACT_INFO"
        const val MAP_LOCATION = "MAP_LOCATION"
        const val GEOFENCE_ENABLED = "GEOFENCE_ENABLED"
        const val SHOW_GEOFENCING = "SHOW_GEOFENCING"
        private const val UNSELECTED_TAB_ALPHA_VIEW = 0.3f
        private const val SELECTED_TAB_ALPHA_VIEW = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this, android.R.color.transparent)
        setContentView(R.layout.store_locator_activity)

        intent?.apply {
            mTitle = getStringExtra(PRODUCT_NAME)
            mDescription = getStringExtra(CONTACT_INFO)

            val mLocationOnMap = getStringExtra(MAP_LOCATION)
            mLocations = Gson().fromJson(mLocationOnMap, object : TypeToken<List<StoreDetails>>() {}.type)
        }

        initViewPagerWithTabLayout()

        ivNavigateBack?.setOnClickListener { onBackPressed() }

        val isInGeoFence = intent?.getBooleanExtra(GEOFENCE_ENABLED, false)
        val participatingStoreDescription = highlightTextInDesc(this, SpannableString(if(isInGeoFence == true) getString(R.string.npc_participating_store)  else  getString(R.string.npc_participating_store_outside_geofence)), "here", true)
        tvStoreContactInfo?.apply {
            val boolean = intent?.getBooleanExtra(SHOW_GEOFENCING, true)
            visibility = if(boolean == false) View.GONE else View.VISIBLE
            text = participatingStoreDescription
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun highlightTextInDesc(context: Context?, spannableTitle: SpannableString, searchTerm: String, textIsClickable: Boolean = true): SpannableString {
        var start = spannableTitle.indexOf(searchTerm)
        if (start == -1) {
            start = 0
        }

        val end = start + searchTerm.length
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

                val intentInStoreFinder = Intent(this@StoreLocatorActivity, StoreSelectAddressActivity::class.java)
                intentInStoreFinder.putExtra(PRODUCT_NAME, bindString(R.string.participating_stores))
                startActivity(intentInStoreFinder)
//                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val typeface: Typeface? = context?.let { ResourcesCompat.getFont(it, R.font.myriad_pro_semi_bold_otf) }

        if (textIsClickable) spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val dimenPix =
                context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
        typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
        spannableTitle.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableTitle
    }

    fun getLocation(): MutableList<StoreDetails>? = mLocations

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
                tvTitle?.text = getString(R.string.participating_stores)
            }
            1 -> {
                tvMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                tvTitle?.text = getString(R.string.nearest_store)
            }
            else -> return
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }
}