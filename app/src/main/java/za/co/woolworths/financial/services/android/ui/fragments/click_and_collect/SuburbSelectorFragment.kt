package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.suburb_selector_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.adapters.SuburbListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.getEnumExtra
import za.co.woolworths.financial.services.android.ui.extension.setDivider
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils
import java.util.EnumSet.of

class SuburbSelectorFragment : Fragment(), SuburbListAdapter.ISuburbSelector {

    private var suburbList: ArrayList<Suburb>? = null
    private var suburbListAdapter: SuburbListAdapter? = null
    private var deliveryType: DeliveryType? = null
    var bundle: Bundle? = null
    var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.suburb_selector_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            getString("SuburbList")?.let {
                suburbList = Gson().fromJson(it, object : TypeToken<List<Suburb>>() {}.type)
            }
            getSerializable("deliveryType")?.let {
                deliveryType = it as DeliveryType
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        if (deliveryType == DeliveryType.DELIVERY) {
            activity?.findViewById<TextView>(R.id.toolbarText)?.text = bindString(R.string.select_your_suburb)
            suburbInputValue.setHint(R.string.hint_search_for_your_suburb)
        } else {
            activity?.findViewById<TextView>(R.id.toolbarText)?.text = bindString(R.string.select_your_store)
            suburbInputValue.setHint(R.string.hint_search_for_your_store)
        }
        suburbInputValue?.apply {
            addTextChangedListener {
                suburbListAdapter?.filter?.filter(it.toString())
            }
        }
        loadSuburbsList()
    }

    private fun loadSuburbsList() {
        rcvSuburbList?.apply {
            suburbListAdapter = suburbList?.let { SuburbListAdapter(it, this@SuburbSelectorFragment, deliveryType) }
            setDivider(R.drawable.recycler_view_divider_gray_1dp)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            suburbListAdapter?.let { adapter = it }
        }
    }

    override fun onSuburbSelected(suburb: Suburb) {
        activity?.apply {
            // Use the Kotlin extension in the fragment-ktx artifact
            val bundle = Bundle()
            bundle?.apply {
                putSerializable("Suburb", suburb)
            }
            setFragmentResult(EditDeliveryLocationFragment.SUBURB_SELECTOR_REQUEST_CODE, bundle)
            navController?.navigateUp()
        }
    }

}