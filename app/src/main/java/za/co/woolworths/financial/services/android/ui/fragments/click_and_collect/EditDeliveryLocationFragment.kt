package za.co.woolworths.financial.services.android.ui.fragments.click_and_collect

import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.click_collect_items_limited_message.*
import kotlinx.android.synthetic.main.edit_delivery_location_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.provinceName
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties.PropertyNames.Companion.storeName
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity.Companion.IS_LIQUOR
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.DeliveryType
import za.co.woolworths.financial.services.android.util.Utils

class EditDeliveryLocationFragment : Fragment(),
    EditDeliveryLocationContract.EditDeliveryLocationView, View.OnClickListener {

    companion object {
        const val SUBURB_SELECTOR_REQUEST_CODE = "1717"
        const val PROVINCE_SELECTOR_REQUEST_CODE = "1818"
        const val SUBURB_LIST = "SuburbList"
        const val SHARED_PREFS = "sharedPrefs"
    }

    var navController: NavController? = null
    var bundle: Bundle? = null
    var regions: List<Province>? = null
    var presenter: EditDeliveryLocationContract.EditDeliveryLocationPresenter? = null
    var selectedProvince: Province? = null
    var selectedSuburb: Suburb? = null
    var selectedStore: Suburb? = null
    var deliveryType: DeliveryType = DeliveryType.DELIVERY
    var validatedSuburbProductsForDelivery: ValidatedSuburbProducts? = null
    var validatedSuburbProductsForStore: ValidatedSuburbProducts? = null
    var rootView: View? = null
    var isLiquor = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null)
            rootView = inflater.inflate(R.layout.edit_delivery_location_fragment, container, false)
        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addFragmentResultListener()
        presenter = EditDeliveryLocationPresenterImpl(this, EditDeliveryLocationInteractorImpl())
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            deliveryType =
                DeliveryType.valueOf(getString(DELIVERY_TYPE, DeliveryType.DELIVERY.name))
            isLiquor = containsKey(IS_LIQUOR)
        }
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(SUBURB_SELECTOR_REQUEST_CODE) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Suburb")
            // Do something with the result
            val suburb: Suburb? = Utils.strToJson(result, Suburb::class.java) as Suburb
            suburb?.let {
                onSuburbSelected(it)
            }
        }
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(PROVINCE_SELECTOR_REQUEST_CODE) { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported
            val result = bundle.getString("Province")
            // Do something with the result
            val province: Province? = Utils.strToJson(result, Province::class.java) as Province
            province?.let {
                onProvinceSelected(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        confirmLocationTextView?.setOnClickListener(this)
        selectProvince?.setOnClickListener(this)
        selectSuburb?.setOnClickListener(this)
        tvSelectedProvince?.setOnClickListener(this)
        tvSelectedSuburb?.setOnClickListener(this)
        tvSelectedProvince?.keyListener = null
        tvSelectedSuburb?.keyListener = null
        delivery?.setOnClickListener(this)
        WoolworthsApplication.getClickAndCollect()?.maxItemsAllowedText?.let {
            maxItemsInfoMessage?.text = it
        }
        setDeliveryOption(deliveryType)
        if (selectedProvince == null && !isLiquor) {
            clickAndCollect?.setOnClickListener(this)
            setUsersCurrentDeliveryDetails()
        } else if (isLiquor) {
            selectedProvince = WoolworthsApplication.getLiquor()?.regions?.get(0)
            onProvinceSelected(selectedProvince)
            // Liquor is only available with Delivery
            disableClickAndCollect()
            // If there is only one region in config
            // change background of province to grey and disable tap
            initLiquorUI()
        }
    }

    private fun initLiquorUI() {
        context?.let {
            when (WoolworthsApplication.getLiquor()?.regions?.size) {
                1 -> {
                    selectProvince?.background = ContextCompat.getDrawable(it, R.drawable.input_box_inactive_bg)
                    tvSelectedProvince?.setBackgroundColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_E5E5E5
                        )
                    )
                    tvSelectedProvince?.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.color_666666
                        )
                    )
                    dropdownGetProvinces?.visibility = View.GONE
                }
                else -> {
                    selectProvince?.background = ContextCompat.getDrawable(it, R.drawable.input_box_active_bg)
                    tvSelectedProvince?.setBackgroundColor(
                        ContextCompat.getColor(
                            it,
                            R.color.white
                        )
                    )
                    tvSelectedProvince?.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.offer_title
                        )
                    )
                    dropdownGetProvinces?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun disableClickAndCollect() {
        context?.let {
            txtclickAndCollect?.setTextColor(ContextCompat.getColor(it, R.color.button_disable))
            newFeature?.setBackgroundColor(ContextCompat.getColor(it, R.color.button_disable))
            foodDeliveryDateMessage?.visibility = View.VISIBLE
            foodDeliveryDateMessage?.text = it.getString(R.string.delivery_liquor_description)
        }
    }

    override fun onClick(v: View?) {
        if (progressGetProvinces.visibility == View.VISIBLE || progressGetSuburb.visibility == View.VISIBLE) return
        when (v?.id) {
            R.id.confirmLocationTextView -> {
                if (selectedSuburb != null || selectedStore != null) {
                    when (deliveryType) {
                        DeliveryType.STORE_PICKUP -> {
                            validatedSuburbProductsForStore.let {
                                when (it) {
                                    null -> executeSetSuburb()
                                    else -> if (it.unSellableCommerceItems.isNullOrEmpty()) executeSetSuburb() else navigateToUnsellableItemsFragment()
                                }
                            }
                        }
                        DeliveryType.DELIVERY -> {
                            validatedSuburbProductsForDelivery.let {
                                when (it) {
                                    null -> executeSetSuburb()
                                    else -> if (it.unSellableCommerceItems.isNullOrEmpty()) executeSetSuburb() else navigateToUnsellableItemsFragment()
                                }
                            }
                        }
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
                navigateToProvinceSelection(it)
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
            var suburbsList = suburbs
            if (isLiquor)
                suburbsList =
                    WoolworthsApplication.getLiquor().suburbs.flatMap { sub -> suburbs.filter { it.id == sub } }
            navigateToSuburbSelection(suburbsList)
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
        if (isLiquor && WoolworthsApplication.getLiquor()?.regions?.size ?: 0 > 1) {
            WoolworthsApplication.getLiquor()?.regions?.let { navigateToProvinceSelection(it) }
            return
        }
        if (progressGetSuburb?.visibility == View.VISIBLE || isLiquor) return
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
        validateConfirmLocationButtonAvailability()
    }

    override fun showGetSuburbProgress() {
        dropdownGetSuburb?.visibility = View.INVISIBLE
        progressGetSuburb?.visibility = View.VISIBLE
        validateConfirmLocationButtonAvailability()
    }

    override fun hideGetProvincesProgress() {
        progressGetProvinces?.visibility = View.INVISIBLE
        dropdownGetProvinces?.visibility = View.VISIBLE
        validateConfirmLocationButtonAvailability()
    }

    override fun hideGetSuburbProgress() {
        progressGetSuburb?.visibility = View.INVISIBLE
        dropdownGetSuburb?.visibility = View.VISIBLE
        validateConfirmLocationButtonAvailability()
    }

    override fun showErrorDialog() {
        val dialog = ErrorDialogFragment.newInstance(
            bindString(R.string.general_error_desc)
                ?: ""
        )
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
            ?.let { fragmentTransaction ->
                dialog.show(
                    fragmentTransaction,
                    ErrorDialogFragment::class.java.simpleName
                )
            }
    }

    override fun onSetSuburbSuccess() {
        hideSetSuburbProgressBar()
        when (deliveryType) {
            DeliveryType.DELIVERY -> {
                Utils.savePreferredDeliveryLocation(
                    ShoppingDeliveryLocation(
                        selectedProvince,
                        selectedSuburb,
                        null
                    )
                )
            }
            DeliveryType.STORE_PICKUP -> {
                val store = selectedStore?.let {
                    Store(
                        it.id,
                        it.name,
                        it.fulfillmentStores,
                        it.storeAddress.address1
                    )
                }
                Utils.savePreferredDeliveryLocation(
                    ShoppingDeliveryLocation(
                        selectedProvince,
                        null,
                        store
                    )
                )
            }
        }
        navigateToSuburbConfirmationFragment()
    }

    override fun onSetSuburbFailure() {
        hideSetSuburbProgressBar()
        showErrorDialog()
    }

    private fun onProvinceSelected(province: Province?) {
        this.selectedProvince = province
        if (!isLiquor || WoolworthsApplication.getLiquor()?.regions?.size ?: 0 > 1)
            resetSuburbSelection()
        tvSelectedProvince?.setText(province?.name)
        tvSelectedProvince?.dismissDropDown()
    }

    private fun onSuburbSelected(suburb: Suburb?) {

        if (deliveryType == DeliveryType.DELIVERY)
            this.selectedSuburb = suburb
        else
            this.selectedStore = suburb
        hideStoreClosedMessage()
        tvSelectedSuburb?.setText(suburb?.name)
        tvSelectedSuburb?.dismissDropDown()
        suburb?.id?.let { validateSelectedSuburb(it, deliveryType == DeliveryType.STORE_PICKUP) }
    }


    private fun resetSuburbSelection() {
        selectedSuburb = null
        selectedStore = null
        validatedSuburbProductsForStore = null
        validatedSuburbProductsForDelivery = null
        tvSelectedSuburb.text.clear()
        tvSelectedSuburb.hint =
            bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.select_a_suburb else R.string.select_a_store)
        validateConfirmLocationButtonAvailability()
        hideAvailableDeliveryDateMessagee()
        hideStoreClosedMessage()
    }

    private fun setDeliveryOption(type: DeliveryType) {
        deliveryType = type
        subTitle?.text =
            bindString(if (deliveryType == DeliveryType.STORE_PICKUP) R.string.select_your_collection_store else R.string.select_your_delivery_location)
        maxItemsInfoMessageLayout?.visibility =
            if (deliveryType == DeliveryType.STORE_PICKUP) View.VISIBLE else View.GONE
        confirmLocationTextView.text =
            bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.confirm_suburb else R.string.confirm_store)
        when (type) {
            DeliveryType.DELIVERY -> {
                clickAndCollect?.setBackgroundResource(R.drawable.delivery_type_store_pickup_un_selected_bg)
                delivery?.setBackgroundColor(Color.BLACK)
                txtDelivery.setTextColor(Color.WHITE)
                txtclickAndCollect.setTextColor(
                    ContextCompat.getColor(
                        WoolworthsApplication.getAppContext(),
                        R.color.offer_title
                    )
                )
                noStoresForProvinceMsg?.visibility = View.GONE
                selectProvince?.setBackgroundResource(R.drawable.input_box_active_bg)
                if (selectedSuburb != null) {
                    tvSelectedSuburb.setText(selectedSuburb?.name)
                } else {
                    tvSelectedSuburb.text.clear()
                    tvSelectedSuburb.hint = bindString(R.string.select_a_suburb)
                }
            }
            DeliveryType.STORE_PICKUP -> {
                clickAndCollect?.setBackgroundColor(Color.BLACK)
                delivery?.setBackgroundResource(R.drawable.delivery_type_delivery_un_selected_bg)
                txtclickAndCollect.setTextColor(Color.WHITE)
                txtDelivery.setTextColor(
                    ContextCompat.getColor(
                        WoolworthsApplication.getAppContext(),
                        R.color.offer_title
                    )
                )
                if (selectedStore != null) {
                    tvSelectedSuburb.setText(selectedStore?.name)
                } else {
                    tvSelectedSuburb.text.clear()
                    tvSelectedSuburb.hint = bindString(R.string.select_a_store)
                }
            }
        }
        showAvailableDeliveryDateMessage()
        validateConfirmLocationButtonAvailability()
    }

    override fun validateConfirmLocationButtonAvailability() {
        if (deliveryType == DeliveryType.DELIVERY)
            confirmLocationTextView?.isEnabled =
                (selectedProvince != null && selectedSuburb != null && progressGetSuburb?.visibility == View.INVISIBLE && progressGetProvinces?.visibility == View.INVISIBLE && !tvSelectedSuburb.text.isNullOrEmpty())
        else
            confirmLocationTextView?.isEnabled =
                (selectedProvince != null && selectedStore != null && progressGetSuburb?.visibility == View.INVISIBLE && progressGetProvinces?.visibility == View.INVISIBLE && !tvSelectedSuburb.text.isNullOrEmpty())
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
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
        }
    }

    override fun validateSelectedSuburb(suburbId: String, isStore: Boolean) {
        if (progressGetProvinces?.visibility == View.VISIBLE) return
        showGetSuburbProgress()
        presenter?.validateSelectedSetSuburb(suburbId, isStore)

    }

    override fun onValidateSelectedSuburbSuccess(validatedSuburbProducts: ValidatedSuburbProducts?) {
        hideGetSuburbProgress()
        if (deliveryType == DeliveryType.DELIVERY)
            this.validatedSuburbProductsForDelivery = validatedSuburbProducts
        else
            this.validatedSuburbProductsForStore = validatedSuburbProducts

        showAvailableDeliveryDateMessage()
    }

    override fun onValidateSelectedSuburbFailure() {
        hideGetSuburbProgress()
        showErrorDialog()
    }

    private fun setUsersCurrentDeliveryDetails() {
        Utils.getPreferredDeliveryLocation()?.apply {
            if (province?.id.isNullOrEmpty()) return
            selectedProvince = province
            tvSelectedProvince?.setText(selectedProvince?.name)
            if (storePickup) {
                selectedStore = Suburb().apply {
                    id = store.id
                    name = store.name
                    fulfillmentStores = store.fulfillmentStores
                    storeAddress = StoreAddress(store.storeAddress)
                }
            } else
                selectedSuburb = suburb
            setDeliveryOption(deliveryType)

            (if (deliveryType == DeliveryType.DELIVERY) selectedSuburb else selectedStore)?.let {
                validateSelectedSuburb(it.id, deliveryType == DeliveryType.STORE_PICKUP)
            }
        }
    }

    private fun showNoStoresError() {
        noStoresForProvinceMsg?.visibility = View.VISIBLE
        noStoresForProvinceMsg.text =
            bindString(R.string.no_stores_for_province_message) + selectedProvince?.name + "."
        selectProvince?.setBackgroundResource(R.drawable.input_error_background)
        tvSelectedSuburb.setText(bindString(R.string.no_stores_available))
    }

    private fun clearNoStoresError() {
        if (noStoresForProvinceMsg?.visibility == View.VISIBLE) {
            noStoresForProvinceMsg?.visibility = View.GONE
            selectProvince?.setBackgroundResource(R.drawable.input_box_active_bg)
            tvSelectedSuburb.text.clear()
            tvSelectedSuburb.hint =
                bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.select_a_suburb else R.string.select_a_store)
        }
    }

    override fun showAvailableDeliveryDateMessage() {
        hideAvailableDeliveryDateMessagee()
        hideStoreClosedMessage()
        (if (deliveryType == DeliveryType.DELIVERY) validatedSuburbProductsForDelivery else validatedSuburbProductsForStore)?.let {
            if (isStoreClosed(it)) {
                showStoreClosedMessage()
            } else {
                when (deliveryType == DeliveryType.STORE_PICKUP) {
                    true -> {
                        earliestDateValue?.text = it.firstAvailableFoodDeliveryDate ?: ""
                        earliestDateValue?.visibility = View.VISIBLE
                        foodItemsDeliveryDateLayout?.visibility = View.GONE
                        otherItemsDeliveryDateLayout?.visibility = View.GONE
                    }
                    false -> {
                        foodItemsDeliveryDate?.text = it.firstAvailableFoodDeliveryDate
                            ?: ""
                        otherItemsDeliveryDate?.text = it.firstAvailableOtherDeliveryDate
                            ?: ""
                        earliestDateValue?.visibility = View.GONE
                        foodItemsDeliveryDateLayout?.visibility =
                            if (it.firstAvailableFoodDeliveryDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                        otherItemsDeliveryDateLayout?.visibility =
                            if (it.firstAvailableOtherDeliveryDate.isNullOrEmpty()) View.GONE else View.VISIBLE
                    }
                }
                earliestDateTitle?.text =
                    bindString(if (deliveryType == DeliveryType.DELIVERY) R.string.earliest_delivery_date else R.string.earliest_collection_date)
                deliveryDateLayout?.visibility =
                    if (!it.firstAvailableFoodDeliveryDate.isNullOrEmpty() || !it.firstAvailableOtherDeliveryDate.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            validateConfirmLocationButtonAvailability()
        }

    }

    override fun hideAvailableDeliveryDateMessagee() {
        deliveryDateLayout?.visibility = View.GONE
    }

    override fun showStoreClosedMessage() {
        storeClosedMsg?.visibility = View.VISIBLE
        selectSuburb?.setBackgroundResource(R.drawable.input_error_background)
    }

    override fun hideStoreClosedMessage() {
        storeClosedMsg?.visibility = View.GONE
        selectSuburb?.setBackgroundResource(R.drawable.input_box_active_bg)
    }

    override fun executeSetSuburb() {
        showSetSuburbProgressBar()
        presenter?.initSetSuburb(if (deliveryType == DeliveryType.DELIVERY) selectedSuburb?.id!! else selectedStore?.id!!)
        if (deliveryType == DeliveryType.STORE_PICKUP) {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_CConfirm)
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_Prov,
                hashMapOf(Pair(provinceName, selectedProvince?.name!!))
            )
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_Click_Collect_Stor,
                hashMapOf(Pair(storeName, selectedStore?.name!!))
            )
        }
    }

    override fun navigateToSuburbConfirmationFragment() {
        bundle?.apply {
            putString(DELIVERY_TYPE, deliveryType.name)
            putString(
                "SUBURB",
                Utils.toJson(if (deliveryType == DeliveryType.DELIVERY) selectedSuburb else selectedStore)
            )
            putString("PROVINCE", Utils.toJson(selectedProvince))
        }
        navController?.navigate(
            R.id.action_to_editDeliveryLocationConfirmationFragment,
            bundleOf("bundle" to bundle)
        )
    }

    override fun navigateToUnsellableItemsFragment() {
        bundle?.apply {
            putString(DELIVERY_TYPE, deliveryType.name)
            putString(
                "SUBURB",
                Utils.toJson(if (deliveryType == DeliveryType.DELIVERY) selectedSuburb else selectedStore)
            )
            putString("PROVINCE", Utils.toJson(selectedProvince))
            putString(
                "UnSellableCommerceItems",
                Utils.toJson((if (deliveryType == DeliveryType.DELIVERY) validatedSuburbProductsForDelivery else validatedSuburbProductsForStore)?.unSellableCommerceItems)
            )
        }
        navController?.navigate(
            R.id.action_to_unsellableItemsFragment,
            bundleOf("bundle" to bundle)
        )
    }

    private fun isStoreClosed(validatedSuburbProducts: ValidatedSuburbProducts?): Boolean {
        val deliveryStatus: HashMap<String, Boolean?>? =
            validatedSuburbProducts?.deliveryStatus?.let {
                Gson().fromJson(
                    it.toString(),
                    object : TypeToken<HashMap<String, Boolean?>>() {}.type
                )
            }
        return (validatedSuburbProducts?.storeClosed == true && deliveryStatus?.get("01") == false)
    }

    override fun navigateToSuburbSelection(suburbs: List<Suburb>) {
        activity?.let {

            // TODO:: WOP-9342 - Handle Transaction too large exception android nougat
            //  and remove share preference temp fix
            val sharedPreferences = it.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putString(SUBURB_LIST, Utils.toJson(suburbs))
            editor?.apply()
            bundle = Bundle()
            bundle?.apply {
                putString("SuburbList", Utils.toJson(suburbs))
                putSerializable("deliveryType", deliveryType)
            }
            navController?.navigate(
                R.id.action_to_suburbSelectorFragment,
                bundleOf("bundle" to bundle)
            )
        }
    }

    override fun navigateToProvinceSelection(regions: List<Province>) {
        bundle = Bundle()
        bundle?.apply {
            putString("ProvinceList", Utils.toJson(regions))
        }
        navController?.navigate(
            R.id.action_to_provinceSelectorFragment,
            bundleOf("bundle" to bundle)
        )
    }

}