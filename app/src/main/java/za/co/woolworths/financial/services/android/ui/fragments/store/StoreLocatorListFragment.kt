package za.co.woolworths.financial.services.android.ui.fragments.store

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.store_locator_list_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.StoreLocatorActivity
import za.co.woolworths.financial.services.android.ui.adapters.StoreLocatorCardListAdapter

class StoreLocatorListFragment : Fragment() {

    companion object {
        fun newInstance() = StoreLocatorListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.store_locator_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val storeLocatorAdapter = StoreLocatorCardListAdapter()
        activity?.let { activity ->
            rvStoreLocator?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            val locations = (activity as? StoreLocatorActivity)?.getLocation() ?: mutableListOf()
            rvStoreLocator?.adapter = storeLocatorAdapter
            storeLocatorAdapter.setItem(locations)
        }
    }
}