package za.co.woolworths.financial.services.android.ui.fragments.product.refine

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentRefinementBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Refinement
import za.co.woolworths.financial.services.android.models.dto.RefinementCrumb
import za.co.woolworths.financial.services.android.models.dto.RefinementNavigation
import za.co.woolworths.financial.services.android.models.dto.RefinementSelectableItem
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.ui.adapters.RefinementAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Response.DyChangeAttributeResponse
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseFragmentListner
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.OnRefinementOptionSelected
import za.co.woolworths.financial.services.android.util.Utils

class RefinementFragment : BaseRefinementFragment(), BaseFragmentListner {
    private lateinit var listener: OnRefinementOptionSelected
    private var refinementAdapter: RefinementAdapter? = null
    private var refinementNavigation: RefinementNavigation? = null
    private var dataList = arrayListOf<RefinementSelectableItem>()
    private var refinedNavigateState = ""
    private var dyReportEventViewModel: DyChangeAttributeViewModel? = null

    companion object {
        private val ARG_PARAM = "refinementNavigationObject"
        private val REFINED_NAVIGATION_STATE = "refinementNavigationState"
        fun getInstance(refinementNavigation: RefinementNavigation, refinedNavigationState: String) = RefinementFragment().withArgs {
            putString(ARG_PARAM, Utils.toJson(refinementNavigation))
            putString(REFINED_NAVIGATION_STATE, refinedNavigationState)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let{
            refinementNavigation = Utils.jsonStringToObject(it.getString(ARG_PARAM), RefinementNavigation::class.java) as RefinementNavigation
            refinedNavigateState = it.getString(REFINED_NAVIGATION_STATE, "")
        }

        try {
            listener = parentFragment as OnRefinementOptionSelected
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initViews()
        dyReportEventViewModel()
    }

    private fun dyReportEventViewModel() {
        dyReportEventViewModel = ViewModelProvider(this).get(DyChangeAttributeViewModel::class.java)
        dyReportEventViewModel!!.getDyLiveData().observe(viewLifecycleOwner, androidx.lifecycle.Observer<DyChangeAttributeResponse?> {
            if (it == null) {
                Log.d(ProductDetailsFragment.TAG, "dyReportEventViewModel: failed to filtertype")
            }else {
                Log.d(ProductDetailsFragment.TAG, "dyReportEventViewModel: successed to filtertype")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_SEARCH_REFINEMENT)
    }

    private fun FragmentRefinementBinding.initViews() {
        listener.apply {
            refinementNavigation?.displayName?.let { setPageTitle(it) }
            hideCloseButton()
        }
        backButton?.setOnClickListener { onBackPressed() }
        clearAndResetFilter?.apply {
            text = getString(R.string.clear_filter)
            setOnClickListener { refinementAdapter?.clearRefinement() }
            visibility = when (refinementNavigation?.multiSelect) {
                true -> View.VISIBLE
                else -> View.INVISIBLE
            }
        }
        refinementSeeResult.setOnClickListener { seeResults() }
        refinementList.layoutManager = LinearLayoutManager(activity)
        loadData()
        onSelectionChanged()
    }

    private fun loadData() {
        dataList = getRefinementSelectableItems(refinementNavigation!!)
        refinementAdapter = activity?.let { RefinementAdapter(it, this, listener, dataList, refinementNavigation!!) }
        binding.refinementList.adapter = refinementAdapter
    }

    private fun getRefinementSelectableItems(refinementNavigation: RefinementNavigation): ArrayList<RefinementSelectableItem> {
        var dataList = arrayListOf<RefinementSelectableItem>()
        if (refinementNavigation.refinementCrumbs != null && refinementNavigation.refinementCrumbs.size > 0) {
            refinementNavigation.refinementCrumbs.forEach {
                var refinementSelectableItem = RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR)
                refinementSelectableItem.isSelected = true
                dataList.add(refinementSelectableItem)
            }
        }

        if (refinementNavigation.refinements != null && refinementNavigation.refinements.size > 0) {
            refinementNavigation.refinements.forEach {
                if (it.displayName.equals("Category", true)) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.CATEGORY))
                } else if (it.subRefinements != null && it.subRefinements.size > 0) {
                    dataList.add(RefinementSelectableItem(it, RefinementSelectableItem.ViewType.OPTIONS))
                } else {
                    dataList.add(RefinementSelectableItem(it, if (it.multiSelect) RefinementSelectableItem.ViewType.MULTI_SELECTOR else RefinementSelectableItem.ViewType.SINGLE_SELECTOR))
                }
            }
        }
        return dataList
    }

    override fun onBackPressed() {
        var navigationState = getNavigationState()
        if (TextUtils.isEmpty(navigationState)) listener.onBackPressedWithOutRefinement() else refinementNavigation?.multiSelect?.let { listener.onBackPressedWithRefinement(navigationState, it) }
    }

    private fun seeResults() =// Prevent creation of new product listing page when user did not multi-select a brand or category
            when(refinementNavigation?.multiSelect == true &&  !isAnyRefinementSelected()){
                true -> refinementNavigation?.multiSelect?.let { listener.onSeeResults(getNavigationState(), it) }
                else  -> refinementNavigation?.multiSelect?.let { listener.onSeeResults(getNavigationState(), it) }
            }

    private fun getNavigationState(): String {
        dataList.forEach {
            var item = it.item
            if (it.type == RefinementSelectableItem.ViewType.MULTI_SELECTOR) {
                if (item is Refinement && it.isSelected) {
                    if (TextUtils.isEmpty(refinedNavigateState))
                        refinedNavigateState = refinedNavigateState.plus(item.navigationState)
                    else
                        refinedNavigateState = refinedNavigateState.plus("Z").plus(item.navigationState.substringAfterLast("Z"))
                } else if (item is RefinementCrumb && !it.isSelected) {
                    refinedNavigateState = refinedNavigateState.replace(getNavigationStateForRefinementCrumb(item.navigationState), "")
                }
            } else if (it.type == RefinementSelectableItem.ViewType.SINGLE_SELECTOR) {
                if (item is Refinement && it.isSelected) {
                    refinedNavigateState = item.navigationState
                } else if (item is RefinementCrumb && !it.isSelected) {
                    refinedNavigateState = refinedNavigateState.replace(getNavigationStateForRefinementCrumb(item.navigationState), "")
                }
            }
        }
        return refinedNavigateState
    }

    override fun onSelectionChanged() {
        binding.clearAndResetFilter?.isEnabled = isAnyRefinementSelected()
        this.updateSeeResultButtonText()
    }

    private fun isAnyRefinementSelected(): Boolean {
        dataList.forEach {
            if (it.isSelected)
                return true
        }
        return false
    }

    private fun getNavigationStateForRefinementCrumb(navigationState: String): String {
        val list = navigationState.substringAfter("Z").split("Z")
        var navigation = refinedNavigateState.substringAfter("Z")
        list.forEachIndexed { index, it ->
            navigation = if (index == 0) navigation.replace(it, "") else navigation.replace("Z".plus(it), "")
        }
        return navigation
    }

    private fun buildSeeResultButtonText(): String {
        var selectedItems = arrayListOf<String>()
        selectedItems.clear()
        dataList.forEach {
            var item = it.item
            if (item is Refinement && it.isSelected) {
                selectedItems.add(item.label)
                AppConfigSingleton.dynamicYieldConfig?.apply {
                    if (isDynamicYieldEnabled == true) {
                        prepareFilterRequestEvent(item.label, item.displayName)
                    }
                }
            } else if (item is RefinementCrumb && it.isSelected) {
                selectedItems.add(item.label)
            }

        }

        return getString(R.string.refinement_see_result_button_text) + if (selectedItems.size > 0) selectedItems.joinToString(",") else refinementNavigation?.displayName
    }

    private fun prepareFilterRequestEvent(label: String, displayName: String) {
        var dyServerId: String? = null
        var dySessionId: String? = null
        var config: NetworkConfig? = null
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null)
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null)
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)

        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress, config?.getDeviceModel())
        val context = za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context(device,null,
            Utils.DY_CHANNEL)
        val properties = Properties(null,null, Utils.FILTER_ITEMS_DY_TYPE,null,null,null,null,null,null,null,null,null,null,null,
            label, displayName,null)
        val eventsDyChangeAttribute = za.co.woolworths.financial.services.android.recommendations.data.response.request.Event(null,null,null,null,null,null,null,null,null,null,null,null,"Filter Items",properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDySortByRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel?.createDyChangeAttributeRequest(prepareDySortByRequestEvent)
    }

    private fun updateSeeResultButtonText() {
        binding.seeResultCount.text = buildSeeResultButtonText()
    }

}
