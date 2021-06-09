package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.province_selector_fragment.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.setDivider
import za.co.woolworths.financial.services.android.util.Utils

class ProvinceSelectorFragment : Fragment(), ProvinceListAdapter.IProvinceSelector {
    private var provinceList: ArrayList<Province>? = null
    var provinceListAdapter: ProvinceListAdapter? = null
    var bundle: Bundle? = null
    var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.province_selector_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            getString("ProvinceList")?.let {
                provinceList = Gson().fromJson(it, object : TypeToken<List<Province>>() {}.type)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        if (activity is CheckoutActivity) {
            (activity as? CheckoutActivity)?.apply { setActionBar() }
        }
        activity?.findViewById<TextView>(R.id.toolbarText)?.text = bindString(R.string.select_your_province)
        loadProvinceList()
    }

    private fun loadProvinceList() {
        rcvProvinceList?.apply {
            provinceListAdapter = provinceList?.let { ProvinceListAdapter(it, this@ProvinceSelectorFragment) }
            setDivider(R.drawable.recycler_view_divider_gray_1dp)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            provinceListAdapter?.let { adapter = it }
        }
    }

    override fun onProvinceSelected(province: Province) {
        activity?.apply {
            // Use the Kotlin extension in the fragment-ktx artifact
            val bundle = Bundle()
            bundle?.apply {
                putString("Province", Utils.toJson(province))
            }
            setFragmentResult(EditDeliveryLocationFragment.PROVINCE_SELECTOR_REQUEST_CODE, bundle)
            navController?.navigateUp()
        }
    }
}