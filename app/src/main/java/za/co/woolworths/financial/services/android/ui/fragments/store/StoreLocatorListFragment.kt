package za.co.woolworths.financial.services.android.ui.fragments.store

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.store_locator_list_fragment.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.vtc.SelectStoreDetailsActivity
import za.co.woolworths.financial.services.android.ui.activities.vtc.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.adapters.StoreLocatorCardListAdapter

class StoreLocatorListFragment : Fragment() {


    private var storeDetailsList: MutableList<StoreDetails>? = ArrayList(0)

    companion object {
        fun newInstance(location: MutableList<StoreDetails>?): StoreLocatorListFragment {
            val fragment = StoreLocatorListFragment()
            fragment.storeDetailsList = location
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.store_locator_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storeLocatorAdapter = StoreLocatorCardListAdapter { storeDetails -> storeDetailItemClick(storeDetails) }
        activity?.let { activity ->
            rvStoreLocator?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            val locations = storeDetailsList ?: ArrayList(0)
            rvStoreLocator?.adapter = storeLocatorAdapter
            storeLocatorAdapter.setItem(locations)
        }
    }

    private fun storeDetailItemClick(storeDetails: StoreDetails) {
        /*activity?.apply {
            with(Intent(this, SelectStoreDetailsActivity::class.java)) {
                putExtra("store", Gson().toJson(storeDetails))
                putExtra("FromStockLocator", false)
                putExtra("SHOULD_DISPLAY_BACK_ICON", true)
                startActivity(this)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }*/
        view?.findNavController()?.navigate(R.id.action_participatingStoreFragment_to_selectStoreDetailsFragment, bundleOf(
                "store" to Gson().toJson(storeDetails),
                "FromStockLocator" to false,
                "SHOULD_DISPLAY_BACK_ICON" to true
        ))
    }
}