package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Context
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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ParticipatingStoreFragmentBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoreLocatorListFragment
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ParticipatingStoreFragment : BaseFragmentBinding<ParticipatingStoreFragmentBinding>(ParticipatingStoreFragmentBinding::inflate) {

    private var mTitle: String? = null
    private var mDescription: String? = null
    private var mLocations: MutableList<StoreDetails>? = null

    companion object {
        const val PRODUCT_NAME = "PRODUCT_NAME"
        const val CONTACT_INFO = "CONTACT_INFO"
        const val MAP_LOCATION = "MAP_LOCATION"
        const val STORE_CARD = "STORE_CARD"
        const val GEOFENCE_ENABLED = "GEOFENCE_ENABLED"
        const val SHOW_GEOFENCING = "SHOW_GEOFENCING"
        const val SHOW_BACK_BUTTON = "SHOW_BACK_BUTTON"
        private const val UNSELECTED_TAB_ALPHA_VIEW = 0.3f
        private const val SELECTED_TAB_ALPHA_VIEW = 1.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.apply {
            mTitle = getString(PRODUCT_NAME)
            mDescription = getString(CONTACT_INFO)

            val mLocationOnMap = getSerializable(MAP_LOCATION)

            mLocations = mLocationOnMap as? MutableList<StoreDetails>
        }
    }

    override fun onResume() {
        super.onResume()
        Utils.hideSoftKeyboard(activity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()
        initViewPagerWithTabLayout()

        //Fetch message from config and display geofencing toast
        val isInGeoFence = arguments?.getBoolean(GEOFENCE_ENABLED, false)
        val geofenceMessage = AppConfigSingleton.instantCardReplacement?.geofencing?.outOfRangeMessages
        geofenceMessage?.let {
            val participatingStoreDescription = highlightTextInDesc(context, SpannableString(if (isInGeoFence == true) it.inRange else it.outOfRange), "here", true)
            binding.tvStoreContactInfo?.apply {
                val boolean = arguments?.getBoolean(SHOW_GEOFENCING, true)
                visibility = if (boolean == false) View.GONE else View.VISIBLE
                text = participatingStoreDescription
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = Color.TRANSPARENT
            }
        }
    }

    private fun setupActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            supportActionBar?.apply {
                show()
                val showBackButton: Boolean = arguments?.getBoolean(SHOW_BACK_BUTTON, false) == true
                setDisplayHomeAsUpEnabled(showBackButton)
                setDisplayUseLogoEnabled(false)
                if (showBackButton) {
                    setHomeAsUpIndicator(R.drawable.back24)
                } else {
                    setHomeAsUpIndicator(null)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (arguments?.getBoolean(SHOW_BACK_BUTTON) == false) {
            inflater.inflate(R.menu.close_menu_item, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item?.itemId) {
            android.R.id.home, R.id.closeIcon -> {
                view?.findNavController()?.navigateUp()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun highlightTextInDesc(context: Context?, spannableTitle: SpannableString, searchTerm: String, textIsClickable: Boolean = true): SpannableString {
        var start = spannableTitle.indexOf(searchTerm)
        if (start == -1) {
            start = 0
        }

        val end = start + searchTerm.length
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                activity?.apply {

                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_VTC_CARD_REPLACEMENT_START, this)

                    view?.findNavController()?.navigate(R.id.action_participatingStoreFragment_to_storeAddressFragment, bundleOf(
                            PRODUCT_NAME to bindString(R.string.participating_stores),
                            STORE_CARD to arguments?.getString(STORE_CARD)
                    ))
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val typeface: Typeface? = context?.let { ResourcesCompat.getFont(it, R.font.opensans_semi_bold) }

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
        binding.vpStoreLocator?.adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> StoreLocatorFragment.newInstance(getLocation(), arguments?.getString(STORE_CARD), arguments?.getBoolean(SHOW_BACK_BUTTON, false) == true)
                    else -> StoreLocatorListFragment.newInstance(getLocation(), arguments?.getString(STORE_CARD), arguments?.getBoolean(SHOW_BACK_BUTTON, false) == true)
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }

        TabLayoutMediator(binding.tabs, binding.vpStoreLocator) { _, _ -> }.attach()

        val tabMapLayout = binding.tabs?.getTabAt(0)
        val tabListLayout = binding.tabs?.getTabAt(1)

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

        binding.vpStoreLocator?.currentItem = 0

        // Disable ViewPager swipe
        binding.vpStoreLocator?.isUserInputEnabled = false

        binding.tabs?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

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
                (activity as? SelectStoreActivity)?.apply {
                    binding.vtcReplacementToolbarTextView?.text = getString(R.string.participating_stores)
                }
            }
            1 -> {
                tvMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                imMapView?.alpha = UNSELECTED_TAB_ALPHA_VIEW
                tvListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                imListView?.alpha = SELECTED_TAB_ALPHA_VIEW
                (activity as? SelectStoreActivity)?.apply {
                    binding.vtcReplacementToolbarTextView?.text = getString(R.string.nearest_store)
                }

            }
            else -> return
        }
    }
}