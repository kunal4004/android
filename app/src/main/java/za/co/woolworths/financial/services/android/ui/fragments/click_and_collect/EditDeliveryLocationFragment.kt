package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.click_collect_items_limited_message.*
import kotlinx.android.synthetic.main.edit_delivery_location_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.provinceName
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.storeName
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.ui.adapters.ProvinceDropdownAdapter
import za.co.woolworths.financial.services.android.ui.adapters.SuburbDropdownAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils
import java.util.HashMap

class EditDeliveryLocationFragment : Fragment(), EditDeliveryLocationContract.EditDeliveryLocationView, View.OnClickListener {

    var navController: NavController? = null
    var bundle: Bundle? = null
    var regions: List<Province>? = null
    var presenter: EditDeliveryLocationContract.EditDeliveryLocationPresenter? = null
    var selectedProvince: Province? = null
    var selectedSuburb: Suburb? = null
    var selectedStore: Suburb? = null
    var deliveryType: DeliveryType = DeliveryType.DELIVERY

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.edit_delivery_location_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = EditDeliveryLocationPresenterImpl(this, EditDeliveryLocationInteractorImpl())
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            deliveryType = DeliveryType.valueOf(getString(DELIVERY_TYPE, DeliveryType.DELIVERY.name))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        confirmLocation?.setOnClickListener(this)
        selectProvince?.setOnClickListener(this)
        selectSuburb?.setOnClickListener(this)
        tvSelectedProvince?.setOnClickListener(this)
        tvSelectedSuburb?.setOnClickListener(this)
        tvSelectedProvince?.keyListener = null
        tvSelectedSuburb?.keyListener = null
        delivery?.setOnClickListener(this)
        clickAndCollect?.setOnClickListener(this)
        confirmLocation?.setOnClickListener(this)
        WoolworthsApplication.getClickAndCollect()?.maxNumberOfItemsAllowed?.let { maxItemsInfoMessage?.text = getString(R.string.click_and_collect_max_items, it.toString()) }
        setDeliveryOption(deliveryType)
        setUsersCurrentDeliveryDetails()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.confirmLocation -> {
                if (selectedSuburb != null || selectedStore != null) {
                    showSetSuburbProgressBar()
                    presenter?.initSetSuburb(if (deliveryType == DeliveryType.DELIVERY) selectedSuburb?.id!! else selectedStore?.id!!)
                    if (deliveryType == DeliveryType.STORE_PICKUP) {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_CConfirm)
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_Prov, hashMapOf(Pair(provinceName, selectedProvince?.name!!)))
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_Stor, hashMapOf(Pair(storeName, selectedStore?.name!!)))
                    }
                }
            }
            R.id.selectProvince, R.id.tvSelectedProvince -> {
                if (selectedSuburb != null || selectedStore != null) resetSuburbSelection()
                clearNoStoresError()
                getProvinces()
            }
            R.id.selectSuburb, R.id.tvSelectedSuburb -> {
                if (selectedProvince == null) return
                getSuburbs()
            }
            R.id.delivery -> setDeliveryOption(DeliveryType.DELIVERY)
            R.id.clickAndCollect -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_Click_Collect)
                setDeliveryOption(DeliveryType.STORE_PICKUP)
            }
        }
    }

    override fun onGetProvincesSuccess(regions: List<Province>) {
        this.regions = regions
        hideGetProvincesProgress()
        activity?.let { activity ->
            this.regions?.let {
                ProvinceDropdownAdapter(activity, 0, it, ::onProvinceSelected).let {
                    tvSelectedProvince?.apply {
                        setAdapter(it)
                        showDropDown()
                    }
                }
            }
        }
    }

    override fun onGetProvincesFailure() {
        hideGetProvincesProgress()
        showErrorDialog()
    }

    override fun onGetSuburbsSuccess(suburbs: List<Suburb>) {
        hideGetSuburbProgress()
        if (suburbs.isNullOrEmpty()) {
            showNoStoresError()
        } else {
            activity?.let { it ->
                SuburbDropdownAdapter(it, 0, suburbs, ::onSuburbSelected).let {
                    tvSelectedSuburb?.apply {
                        setAdapter(it)
                        showDropDown()
                    }
                }
            }
        }
    }

    override fun onGetSuburbsFailure() {
        hideGetSuburbProgress()
        showErrorDialog()
    }

    override fun onGenericFailure() {
        hideGetSuburbProgress()
        hideGetProvincesProgress()
        hideSetSuburbProgressBar()
        showErrorDialog()
    }

    override fun getProvinces() {
        if (progressGetSuburb?.visibility == View.VISIBLE) return
        showGetProvincesProgress()
        presenter?.initGetProvinces()
    }

    override fun getSuburbs() {
        if (progressGetProvinces?.visibility == View.VISIBLE) return
        showGetSuburbProgress()
        selectedProvince?.id?.let { presenter?.initGetSuburbs(it, deliveryType) }
    }

    override fun showGetProvincesProgress() {
        dropdownGetProvinces?.visibility = View.INVISIBLE
        progressGetProvinces?.visibility = View.VISIBLE
    }

    override fun showGetSuburbProgress() {
        dropdownGetSuburb?.visibility = View.INVISIBLE
        progressGetSuburb?.visibility = View.VISIBLE
    }

    override fun hideGetProvincesProgress() {
        progressGetProvinces?.visibility = View.INVISIBLE
        dropdownGetProvinces?.visibility = View.VISIBLE
    }

    override fun hideGetSuburbProgress() {
        progressGetSuburb?.visibility = View.INVISIBLE
        dropdownGetSuburb?.visibility = View.VISIBLE
    }

    override fun showErrorDialog() {
        val dialog = ErrorDialogFragment.newInstance(bindString(R.string.general_error_desc)
                ?: "")
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    override fun onSetSuburbSuccess() {
        hideSetSuburbProgressBar()
        Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(selectedProvince, if (deliveryType == DeliveryType.DELIVERY) selectedSuburb else selectedStore))
        bundle?.putString(DELIVERY_TYPE, deliveryType.name)
        bundle?.putString("SUBURB", Utils.toJson(if (deliveryType == DeliveryType.DELIVERY) selectedSuburb else selectedStore))
        bundle?.putString("PROVINCE", Utils.toJson(selectedProvince))
        navController?.navigate(R.id.action_to_editDeliveryLocationConfirmationFragment, bundleOf("bundle" to bundle))
    }

    override fun onSetSuburbFailure() {
        hideSetSuburbProgressBar()
        showErrorDialog()
    }

    private fun onProvinceSelected(province: Province?) {
        this.selectedProvince = province
        tvSelectedProvince?.setText(province?.name)
        tvSelectedProvince?.dismissDropDown()
    }

    private fun onSuburbSelected(suburb: Suburb?) {

        if (deliveryType == DeliveryType.DELIVERY)
            this.selectedSuburb = suburb
        else
            this.selectedStore = suburb
        tvSelectedSuburb?.setText(suburb?.name)
        tvSelectedSuburb?.dismissDropDown()
        validateConfirmLocationButtonAvailability()
    }


    private fun resetSuburbSelection() {
        selectedSuburb = null
        selectedStore = null
        tvSelectedSuburb.text.clear()
        tvSelectedSuburb.hint = bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.select_a_suburb else R.string.select_a_store)
        validateConfirmLocationButtonAvailability()
    }

    private fun setDeliveryOption(type: DeliveryType) {
        deliveryType = type
        subTitle?.text = bindString(if (deliveryType == DeliveryType.STORE_PICKUP) R.string.select_your_collection_store else R.string.select_your_delivery_location)
        maxItemsInfoMessageLayout?.visibility = if (deliveryType == DeliveryType.STORE_PICKUP) View.VISIBLE else View.GONE
        when (type) {
            DeliveryType.DELIVERY -> {
                clickAndCollect?.setBackgroundResource(R.drawable.delivery_type_store_pickup_un_selected_bg)
                delivery?.setBackgroundResource(R.drawable.onde_dp_black_border_bg)
                noStoresForProvinceMsg?.visibility = View.GONE
                selectProvince?.setBackgroundResource(R.drawable.input_box_inactive_bg)
                if (selectedSuburb != null) {
                    tvSelectedSuburb.setText(selectedSuburb?.name)
                } else {
                    tvSelectedSuburb.text.clear()
                    tvSelectedSuburb.hint = bindString(R.string.select_a_suburb)
                }
            }
            DeliveryType.STORE_PICKUP -> {
                clickAndCollect?.setBackgroundResource(R.drawable.onde_dp_black_border_bg)
                delivery?.setBackgroundResource(R.drawable.delivery_type_delivery_un_selected_bg)
                if (selectedStore != null) {
                    tvSelectedSuburb.setText(selectedStore?.name)
                } else {
                    tvSelectedSuburb.text.clear()
                    tvSelectedSuburb.hint = bindString(R.string.select_a_store)
                }
            }
        }
        validateConfirmLocationButtonAvailability()
    }

    override fun validateConfirmLocationButtonAvailability() {
        if (deliveryType == DeliveryType.DELIVERY)
            confirmLocation?.isEnabled = (selectedProvince != null && selectedSuburb != null)
        else
            confirmLocation?.isEnabled = (selectedProvince != null && selectedStore != null)
    }

    override fun hideSetSuburbProgressBar() {
        progressSetSuburb?.visibility = View.INVISIBLE
        activity?.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    override fun showSetSuburbProgressBar() {
        progressSetSuburb?.visibility = View.VISIBLE
        activity?.apply {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private fun setUsersCurrentDeliveryDetails() {
        Utils.getPreferredDeliveryLocation()?.apply {
            if (province?.id.isNullOrEmpty()) return
            selectedProvince = province
            tvSelectedProvince?.setText(selectedProvince?.name)
            if (suburb.storePickup)
                selectedStore = suburb
            else
                selectedSuburb = suburb
            setDeliveryOption(deliveryType)
        }
    }

    private fun showNoStoresError() {
        noStoresForProvinceMsg?.visibility = View.VISIBLE
        noStoresForProvinceMsg.text =bindString(R.string.no_stores_for_province_message)+selectedProvince?.name+"."
        selectProvince?.setBackgroundResource(R.drawable.input_error_background)
        tvSelectedSuburb.setText(bindString(R.string.no_stores_available))
    }

    private fun clearNoStoresError() {
        if (noStoresForProvinceMsg?.visibility == View.VISIBLE) {
            noStoresForProvinceMsg?.visibility = View.GONE
            selectProvince?.setBackgroundResource(R.drawable.input_box_inactive_bg)
            tvSelectedSuburb.text.clear()
            tvSelectedSuburb.hint = bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.select_a_suburb else R.string.select_a_store)
        }
    }


}