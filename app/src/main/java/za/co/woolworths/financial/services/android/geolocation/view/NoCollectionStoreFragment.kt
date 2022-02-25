package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.no_collection_store_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity

class NoCollectionStoreFragment: Fragment(R.layout.no_collection_store_fragment){

    companion object {
        fun newInstance() = NoCollectionStoreFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_change_location?.setOnClickListener {
            (activity as? BottomNavigationActivity)?.pushFragment(ConfirmAddressFragment.newInstance())
        }
    }
}