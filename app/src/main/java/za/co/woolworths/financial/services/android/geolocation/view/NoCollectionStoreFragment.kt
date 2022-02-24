package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R

class NoCollectionStoreFragment: Fragment(R.layout.no_collection_store_fragment){

    companion object {
        fun newInstance() = NoCollectionStoreFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}