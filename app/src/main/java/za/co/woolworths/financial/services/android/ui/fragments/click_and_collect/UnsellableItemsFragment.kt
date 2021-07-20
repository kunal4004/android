package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.unsellable_items_fragment.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.adapters.UnsellableItemsListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.Utils

class UnsellableItemsFragment : Fragment(), View.OnClickListener {

    var selectedSuburb: Suburb? = null
    var selectedProvince: Province? = null
    var deliveryType: DeliveryType? = null
    var bundle: Bundle? = null
    private var commerceItems: ArrayList<UnSellableCommerceItem>? = null
    var navController: NavController? = null

    companion object {
        const val KEY_ARGS_BUNDLE = "bundle"
        const val KEY_BUNDLE_SUBURB = "SUBURB"
        const val KEY_BUNDLE_PROVINCE = "PROVINCE"
        const val KEY_BUNDLE_UNSELLABLE_COMMERCE_ITEMS = "UnSellableCommerceItems"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(KEY_ARGS_BUNDLE)
        bundle?.apply {
            deliveryType = DeliveryType.valueOf(getString(EditDeliveryLocationActivity.DELIVERY_TYPE, DeliveryType.DELIVERY.name))
            selectedSuburb = Utils.jsonStringToObject(getString(KEY_BUNDLE_SUBURB), Suburb::class.java) as Suburb?
            selectedProvince = Utils.jsonStringToObject(getString(KEY_BUNDLE_PROVINCE), Province::class.java) as Province?
            commerceItems = Gson().fromJson(getString(KEY_BUNDLE_UNSELLABLE_COMMERCE_ITEMS), object : TypeToken<List<UnSellableCommerceItem>>() {}.type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.unsellable_items_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        removeItems?.setOnClickListener(this)
        changeStore?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@UnsellableItemsFragment)
        }
        loadUnsellableItems()
    }

    private fun loadUnsellableItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = UnsellableItemsListAdapter(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.changeStore -> {
                setFragmentResult(CheckoutAddressConfirmationFragment.UNSELLABLE_CHANGE_STORE_REQUEST_KEY, Bundle())
                navController?.navigateUp()
            }
            R.id.removeItems -> executeSetSuburb()
        }
    }

    private fun executeSetSuburb() {
        showSetSuburbProgressBar()
        selectedSuburb?.let {
            OneAppService.setSuburb(it.id).enqueue(CompletionHandler(object : IResponseListener<SetDeliveryLocationSuburbResponse> {
                override fun onSuccess(response: SetDeliveryLocationSuburbResponse?) {
                    hideSetSuburbProgressBar()
                    when (response?.httpCode) {
                        200 -> {
                            QueryBadgeCounter.instance.queryCartSummaryCount()
                            when (deliveryType) {
                                DeliveryType.DELIVERY -> Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(selectedProvince, it, null))
                                DeliveryType.STORE_PICKUP -> {
                                    Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(selectedProvince, null, Store(it.id, it.name, it.fulfillmentStores, it.storeAddress.address1)))
                                }
                            }
                            if (activity is CheckoutActivity) {
                                setFragmentResult(CheckoutAddressConfirmationFragment.UNSELLABLE_CHANGE_STORE_REQUEST_KEY, Bundle())
                                navController?.navigateUp()
                            } else
                                navigateToSuburbConfirmationFragment()
                        }
                        else -> {
                            showErrorDialog()
                        }
                    }

                }

                override fun onFailure(error: Throwable?) {
                    super.onFailure(error)
                    showErrorDialog()
                }
            }, SetDeliveryLocationSuburbResponse::class.java))
        }
    }

    fun navigateToSuburbConfirmationFragment() {
        bundle?.apply {
            putString(EditDeliveryLocationActivity.DELIVERY_TYPE, deliveryType?.name)
            putString("SUBURB", Utils.toJson(selectedSuburb))
            putString("PROVINCE", Utils.toJson(selectedProvince))
        }
        navController?.navigate(R.id.action_unsellableItemsFragment_to_editDeliveryLocationConfirmationFragment, bundleOf("bundle" to bundle))
    }

    fun showErrorDialog() {
        hideSetSuburbProgressBar()
        val dialog = ErrorDialogFragment.newInstance(bindString(R.string.general_error_desc)
                ?: "")
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    fun hideSetSuburbProgressBar() {
        progressSetSuburb?.visibility = View.INVISIBLE
        activity?.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    fun showSetSuburbProgressBar() {
        progressSetSuburb?.visibility = View.VISIBLE
        activity?.apply {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}