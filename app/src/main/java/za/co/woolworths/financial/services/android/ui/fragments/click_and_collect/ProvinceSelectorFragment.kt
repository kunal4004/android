package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.checkout_address_confirmation_click_and_collect.*
import kotlinx.android.synthetic.main.province_selector_fragment.*
import kotlinx.android.synthetic.main.province_selector_fragment.loadingProgressBar
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddAddressNewUserFragment.Companion.PROVINCE_SELECTION_BACK_PRESSED
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.SuburbsResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.setDivider
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class ProvinceSelectorFragment : Fragment(), ProvinceListAdapter.IProvinceSelector {
    private var provinceList: ArrayList<Province>? = null
    var provinceListAdapter: ProvinceListAdapter? = null
    var bundle: Bundle? = null
    var navController: NavController? = null
    var isCheckoutChangeLocation = false
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel

    companion object {
        const val CHECKOUT_CHANGE_LOCATION_KEY = "isCheckoutChangeLocation"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.province_selector_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            getString("ProvinceList")?.let {
                provinceList = Gson().fromJson(it, object : TypeToken<List<Province>>() {}.type)
            }
            if (containsKey(CHECKOUT_CHANGE_LOCATION_KEY)) {
                isCheckoutChangeLocation = getBoolean(CHECKOUT_CHANGE_LOCATION_KEY)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        if (activity is CheckoutActivity) {
            setHasOptionsMenu(true)
            (activity as? CheckoutActivity)?.apply { hideBackArrow() }
        }
        activity?.findViewById<TextView>(R.id.toolbarText)?.text =
            bindString(R.string.select_your_province)
        loadProvinceList()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_item, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    private fun loadProvinceList() {
        rcvProvinceList?.apply {
            provinceListAdapter =
                provinceList?.let { ProvinceListAdapter(it, this@ProvinceSelectorFragment) }
            setDivider(R.drawable.recycler_view_divider_gray_1dp)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            provinceListAdapter?.let { adapter = it }
        }
    }

    override fun onProvinceSelected(province: Province) {
        if (isCheckoutChangeLocation) {
            setupViewModel()
            getSuburb(province)
        } else {
            activity?.apply {
                // Use the Kotlin extension in the fragment-ktx artifact
                val bundle = Bundle()
                bundle?.apply {
                    putString("Province", Utils.toJson(province))
                }
                setFragmentResult(
                    EditDeliveryLocationFragment.PROVINCE_SELECTOR_REQUEST_CODE,
                    bundle
                )
                navController?.navigateUp()
            }
        }
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }

    private fun getSuburb(province: Province?) {
        province?.id?.let {
            checkoutAddAddressNewUserViewModel.initGetSuburbs(it).observe(viewLifecycleOwner, {
                when (it.responseStatus) {
                    ResponseStatus.SUCCESS -> {
                        loadingProgressBar.visibility = View.GONE
                        if ((it?.data as? SuburbsResponse)?.suburbs.isNullOrEmpty()) {
                            //showNoStoresError()
                        } else {
                            (it?.data as? SuburbsResponse)?.suburbs?.let { it1 ->
                                val bundle = Bundle()
                                bundle.apply {
                                    putString("SuburbList", Utils.toJson(it1))
                                    putSerializable("deliveryType", DeliveryType.DELIVERY)
                                }
                                navController?.navigate(
                                    R.id.action_suburbSelectorFragment,
                                    bundleOf("bundle" to bundle)
                                )
                            }
                        }
                    }
                    ResponseStatus.LOADING -> {
                        loadingProgressBar.visibility = View.VISIBLE
                    }
                    ResponseStatus.ERROR -> {
                        loadingProgressBar.visibility = View.GONE
                    }
                }
            })
        }
    }

    fun onBackPressed() {
        activity?.apply {
            // Use the Kotlin extension in the fragment-ktx artifact
            setFragmentResult(PROVINCE_SELECTION_BACK_PRESSED, Bundle())
        }
    }
}