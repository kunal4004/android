package za.co.woolworths.financial.services.android.geolocation.view

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentClickAndCollectStoresBinding
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_CNC
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_STORE_LOCATOR
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.NEED_STORE_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper.switchDeliverModeEvent
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseDialogFragmentBinding
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@AndroidEntryPoint
class ClickAndCollectStoresFragment :
    BaseDialogFragmentBinding<FragmentClickAndCollectStoresBinding>(
        FragmentClickAndCollectStoresBinding::inflate
    ),
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher, VtoTryAgainListener {

    private var mValidateLocationResponse: ValidateLocationResponse? = null
    private var dataStore: Store? = null
    private var bundle: Bundle? = null
    private var validateLocationResponse: ValidateLocationResponse? = null
    private var placeId: String? = null
    private var isComingFromConfirmAddress: Boolean? = false
    private var isComingFromNewToggleFulfilment: Boolean? = false
    private var needStoreSelection: Boolean? = false
    private var unSellableCommerceItems: List<UnSellableCommerceItem> = emptyList()
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var whoIsCollectingDetails: WhoIsCollectingDetails? = null

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    companion object {
        fun newInstance(bundle: Bundle?) =
            ClickAndCollectStoresFragment().withArgs {
                this.putBundle(BUNDLE, bundle)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            placeId = this.getString(KEY_PLACE_ID, "")
            isComingFromNewToggleFulfilment = this.getBoolean(IS_COMING_FROM_NEW_TOGGLE_FULFILMENT_SCREEN, false)
            needStoreSelection = this.getBoolean(NEED_STORE_SELECTION, false)
            isComingFromConfirmAddress = getBoolean(IS_COMING_CONFIRM_ADD, false)
            isComingFromSlotSelection = getBoolean(BundleKeysConstants.IS_COMING_FROM_SLOT_SELECTION, false) ?: false
            isComingFromCheckout = getBoolean(BundleKeysConstants.IS_COMING_FROM_CHECKOUT, false) ?: false
            getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS, null)?.let {
                whoIsCollectingDetails = Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
            }
            if (containsKey(VALIDATE_RESPONSE)) {
                getSerializable(VALIDATE_RESPONSE)?.let {
                    mValidateLocationResponse =
                        it as ValidateLocationResponse
                }

            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            tvConfirmStore?.setOnClickListener(this@ClickAndCollectStoresFragment)
            btChange?.setOnClickListener(this@ClickAndCollectStoresFragment)
            backButton?.setOnClickListener(this@ClickAndCollectStoresFragment)
            etEnterNewAddress?.addTextChangedListener(this@ClickAndCollectStoresFragment)
            dialog?.window
                ?.attributes?.windowAnimations = R.style.DialogFragmentAnimation
            if (isComingFromConfirmAddress == true || (needStoreSelection == true && mValidateLocationResponse == null)) {
                placeId?.let {
                    if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                        getDeliveryDetailsFromValidateLocation(it)
                        noClickAndCollectConnectionLayout?.noConnectionLayout?.visibility =
                            View.GONE
                    } else {
                        noClickAndCollectConnectionLayout?.noConnectionLayout?.visibility =
                            View.VISIBLE
                    }
                }
            } else {
                setAddressUI(
                    mValidateLocationResponse?.validatePlace?.stores,
                    mValidateLocationResponse
                )
            }
        }
        addObserver()
    }


    private fun setAddressUI(
        address: List<Store>?,
        mValidateLocationResponse: ValidateLocationResponse?,
    ) {
        binding.apply {
            tvStoresNearMe?.text = resources.getString(R.string.near_stores, address?.size)
            tvAddress?.text =
                KotlinUtils.capitaliseFirstLetter(mValidateLocationResponse?.validatePlace?.placeDetails?.address1)
            setStoreList(address)
        }
    }

    private fun setStoreList(address: List<Store>?) {
        binding.apply {
            rvStoreList.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            val storesListWithHeaders =
                StoreUtils.getStoresListWithHeaders(StoreUtils.sortedStoreList(address))

            if (storesListWithHeaders.isNotEmpty()) {
                tvConfirmStore?.isEnabled = false
                rvStoreList.adapter = activity?.let { activity ->
                    StoreListAdapter(
                        activity,
                        storesListWithHeaders,
                        this@ClickAndCollectStoresFragment
                    )
                }
                rvStoreList.runWhenReady {
                    lifecycleScope.launch {
                        if (!AppInstanceObject.get().featureWalkThrough.new_fbh_cnc) {
                            delay(2000)
                            firstTimeFBHCNCIntroDialog()
                        }
                    }
                }
            }
            rvStoreList.adapter?.notifyDataSetChanged()
        }
    }

    private fun addObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            confirmAddressViewModel.validateStoreInventoryData.collectLatest { validatePlaceResponse ->
                with(validatePlaceResponse) {
                    renderLoading {
                        if (isLoading) {
                            binding.clickCollectProgress.visibility = View.VISIBLE
                        } else
                            binding.clickCollectProgress.visibility = View.GONE
                    }
                    renderSuccess {
                        setBrowsingDataInformation(output)


                    }
                }
            }
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            object : Dialog(it, theme) {
                override fun onBackPressed() {
                    closeDialog()
                }
            }
        } ?: super.onCreateDialog(savedInstanceState)
    }

    private fun setBrowsingDataInformation(validateStoreResponse: ValidateStoreResponse) {
        val browsingStoreList = validateStoreResponse?.validatePlace?.stores
        if (!browsingStoreList.isNullOrEmpty()) {
            dataStore = browsingStoreList[0]
            val storeData = WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores
                ?: WoolworthsApplication.getValidatePlaceDetails()?.stores
            storeData?.forEach { listStore ->
                if (listStore.storeId == browsingStoreList[0].storeId) {
                    KotlinUtils.setCncStoreValidateResponse(browsingStoreList[0], listStore)
                    unSellableCommerceItems= browsingStoreList[0].unSellableCommerceItems!!
                    return@forEach
                }

            }
        }
        navigateToFulfillmentScreen()

    }
    private fun callValidateStoreInventory() {
        lifecycleScope.launch {
            if (placeId.isNullOrEmpty() && dataStore?.storeId.isNullOrEmpty()) {
                return@launch
            } else {
                confirmAddressViewModel.queryValidateStoreInventory(placeId!!, dataStore?.storeId!!)
            }
        }
    }

    override fun onStoreSelected(mStore: Store?) {
        dataStore = mStore
        binding.tvConfirmStore?.isEnabled = true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_STORE,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_STORE
                    ),
                    activity
                )
                callValidateStoreInventory()
            }

            R.id.backButton -> {
                closeDialog()
            }

            R.id.btChange -> {
                IS_FROM_STORE_LOCATOR = true
                findNavController().navigate(
                    R.id.action_clickAndCollectStoresFragment_to_confirmAddressLocationFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        }
    }

    private fun closeDialog() {
        if (needStoreSelection == true) {
            activity?.finish()
        } else {
            dismiss()
        }
    }

    private fun navigateToFulfillmentScreen() {
        if (isComingFromNewToggleFulfilment == true) {
            if(unSellableCommerceItems?.size!!>0){
                sendResultBack()
            } else {
                (mValidateLocationResponse ?: validateLocationResponse)?.let { confirmSetAddress(it) }
            }
        } else if (IS_FROM_STORE_LOCATOR) {
            dataStore?.let {
                bundle?.putString(
                    KEY_PLACE_ID, placeId
                )
                IS_FROM_STORE_LOCATOR = false
                setFragmentResult(
                    DeliveryAddressConfirmationFragment.STORE_LOCATOR_REQUEST_CODE,
                    bundleOf(BUNDLE to it)
                )
            }
            findNavController().navigate(
                R.id.action_clickAndCollectStoresFragment_to_deliveryAddressConfirmationFragment,
                bundleOf(BUNDLE to bundle)
            )
        } else {
            dataStore?.let {
                setFragmentResult(
                    DeliveryAddressConfirmationFragment.STORE_LOCATOR_REQUEST_CODE,
                    bundleOf(BUNDLE to it)
                )
            }
            dismiss()
        }
    }

    private fun confirmSetAddress(validateLocationResponse: ValidateLocationResponse) {
        if (placeId.isNullOrEmpty())
            return

        val confirmAddress = ConfirmLocationAddress(placeId = placeId)
        val confirmLocationRequest = ConfirmLocationRequest(
            deliveryType = BundleKeysConstants.CNC,
            storeId = dataStore?.storeId,
            address = confirmAddress
        )

        lifecycleScope.launch {
            binding.clickCollectProgress?.visibility = View.VISIBLE
            try {
                val confirmLocationResponse =
                    confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                binding.clickCollectProgress?.visibility = View.GONE
                if (!isAdded || !isVisible) return@launch

                when (confirmLocationResponse.httpCode) {
                    AppConstant.HTTP_OK -> {

                        /*reset browsing data for cnc and dash both once fulfillment location is confirmed*/
                        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                            validateLocationResponse?.validatePlace
                        )
                        WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
                            validateLocationResponse?.validatePlace
                        )

                        KotlinUtils.placeId = placeId
                        KotlinUtils.isLocationPlaceIdSame =
                            placeId?.equals(Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId)

                        WoolworthsApplication.setValidatedSuburbProducts(
                            validateLocationResponse.validatePlace
                        )

                        // save details in cache
                        if (SessionUtilities.getInstance().isUserAuthenticated) {
                            Utils.savePreferredDeliveryLocation(
                                ShoppingDeliveryLocation(
                                    confirmLocationResponse.orderSummary?.fulfillmentDetails
                                )
                            )
                            if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                KotlinUtils.clearAnonymousUserLocationDetails()
                        } else {
                            KotlinUtils.saveAnonymousUserLocationDetails(
                                ShoppingDeliveryLocation(
                                    confirmLocationResponse.orderSummary?.fulfillmentDetails
                                )
                            )
                        }

                        onConfirmLocationNavigation()
                    }
                }
            } catch (e: Exception) {
                if (!isAdded || !isVisible) return@launch

                binding.clickCollectProgress?.visibility = View.GONE
                FirebaseManager.logException(e)
                showErrorDialog()
            }
        }
    }

    private fun onConfirmLocationNavigation() {
        if (isComingFromCheckout && isComingFromSlotSelection) {
            val deliveryType = Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
            if (deliveryType == Delivery.CNC) {
                startCheckoutActivity(Utils.toJson(whoIsCollectingDetails))
            } else {
                sendResult()
            }
        } else {
            sendResult()
        }
    }

    private fun startCheckoutActivity(toJson: String?) {
        val checkoutActivityIntent = Intent(requireActivity(), CheckoutActivity::class.java).apply {
            putExtra(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS, toJson)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
            putExtra(Constant.NO_LIQUOR_IMAGE_URL, getLiquorImageUrl())
        }
        requireActivity().apply {
            startActivityForResult(checkoutActivityIntent, CartFragment.REQUEST_PAYMENT_STATUS)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out_to_left)
            finish()
        }
    }

    private fun getLiquorOrder(): Boolean {
        var liquorOrder = false
        bundle?.apply {
            liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
        }
        return liquorOrder
    }

    private fun getLiquorImageUrl(): String {
        var liquorImageUrl = ""
        bundle?.apply {
            liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL, "")
        }
        return liquorImageUrl
    }

    private fun sendResult() {
        switchDeliverModeEvent(Delivery.CNC.toString())
        activity?.setResult(Activity.RESULT_OK)
        activity?.finish()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // not required
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // not required
    }

    override fun afterTextChanged(s: Editable?) {
        val list = ArrayList<Store>()
        mValidateLocationResponse?.validatePlace?.stores?.let {
            for (store in it) {
                if (store.storeName?.contains(
                        s.toString(),
                        true
                    ) == true || store.storeAddress?.contains(s.toString(), true) == true
                ) {
                    list.add(store)
                }
            }
        }
        if (list.isNotEmpty()) {
            setStoreList(list)
        }
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return
        viewLifecycleOwner.lifecycleScope.launch {
            binding.apply {
                clickCollectProgress?.visibility = View.VISIBLE
                try {
                    validateLocationResponse =
                        confirmAddressViewModel.getValidateLocation(placeId)
                    clickCollectProgress?.visibility = View.GONE
                    if (validateLocationResponse != null) {
                        when (validateLocationResponse?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                setAddressUI(
                                    validateLocationResponse?.validatePlace?.stores,
                                    validateLocationResponse
                                )
                            }

                            else -> {
                                showErrorDialog()
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    clickCollectProgress?.visibility = View.GONE
                    showErrorDialog()
                } catch (e: JsonSyntaxException) {
                    FirebaseManager.logException(e)
                    clickCollectProgress?.visibility = View.GONE
                    showErrorDialog()
                }
            }
        }
    }

    private fun showErrorDialog() {
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@ClickAndCollectStoresFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    override fun tryAgain() {
        if (confirmAddressViewModel.isConnectedToInternet(requireActivity()))
            placeId?.let { getDeliveryDetailsFromValidateLocation(it) }
    }

    override fun onFirstTimePargo() {
        findNavController().navigate(R.id.action_clickAndCollectStoresFragment_to_pargoStoreInfoBottomSheetDialog)
    }

    private fun firstTimeFBHCNCIntroDialog() {
        val fbh = FBHInfoBottomSheetDialog()
        activity?.supportFragmentManager?.let { fbh.show(it, AppConstant.TAG_FBH_CNC_FRAGMENT) }
    }

    private fun sendResultBack() {
        val  deliveryType: Delivery=Delivery.CNC
        //unSellableCommerceItems = store.unSellableCommerceItems!!
        //  if(unSellableCommerceItems?.size!!>0)

        val intent = Intent().apply {
            putExtra(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE,
                ShopToggleActivity.ToggleFulfilmentWIthUnsellable(
                    unSellableCommerceItems,
                    deliveryType
                )
            )
            putExtra(DELIVERY_CNC, deliveryType)
        }

        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()



    }

}