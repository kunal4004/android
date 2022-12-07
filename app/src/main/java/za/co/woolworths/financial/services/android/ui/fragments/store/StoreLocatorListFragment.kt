package za.co.woolworths.financial.services.android.ui.fragments.store

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreLocatorListFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.adapters.StoreLocatorCardListAdapter
import za.co.woolworths.financial.services.android.ui.fragments.npc.ParticipatingStoreFragment.Companion.STORE_CARD
import za.co.woolworths.financial.services.android.ui.fragments.vtc.SelectStoreDetailsFragment
import za.co.woolworths.financial.services.android.util.Utils

class StoreLocatorListFragment : Fragment(R.layout.store_locator_list_fragment) {

    private lateinit var binding: StoreLocatorListFragmentBinding
    private var storeDetailsList: MutableList<StoreDetails>? = ArrayList(0)
    private var showStoreSelect: Boolean = false

    companion object {
        fun newInstance(location: MutableList<StoreDetails>?, storeCardDetails: String?, showStoreSelect: Boolean): StoreLocatorListFragment {
            val fragment = StoreLocatorListFragment()
            fragment.arguments = bundleOf(
                    STORE_CARD to storeCardDetails
            )
            fragment.storeDetailsList = location
            fragment.showStoreSelect = showStoreSelect
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = StoreLocatorListFragmentBinding.bind(view)

        val storeLocatorAdapter = StoreLocatorCardListAdapter { storeDetails -> storeDetailItemClick(storeDetails) }
        activity?.let { activity ->
            binding.rvStoreLocator?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            val locations = storeDetailsList ?: ArrayList(0)
            binding.rvStoreLocator?.adapter = storeLocatorAdapter
            storeLocatorAdapter.setItem(locations)
        }
    }

    private fun storeDetailItemClick(storeDetails: StoreDetails) {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_SC_REPLACE_CARD_STORE, this) }

        view?.findNavController()?.navigate(R.id.action_participatingStoreFragment_to_selectStoreDetailsFragment, bundleOf(
                "store" to Gson().toJson(storeDetails),
                STORE_CARD to arguments?.getString(STORE_CARD),
                SelectStoreDetailsFragment.SHOW_STORE_SELECT to showStoreSelect,
                "FromStockLocator" to false,
                "SHOULD_DISPLAY_BACK_ICON" to true
        ))
    }
}