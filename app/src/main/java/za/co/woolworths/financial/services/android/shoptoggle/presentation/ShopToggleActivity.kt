package za.co.woolworths.financial.services.android.shoptoggle.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutActivity
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressManagementBaseFragment
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.geolocation.GeoUtils
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.LiquorCompliance
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.presentation.components.ShopToggleScreen
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.Constant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class ShopToggleActivity : ComponentActivity() {
    var delivery: Delivery? = null

    companion object {
        const val REQUEST_DELIVERY_TYPE = 105
        const val REQUEST_DESTROY_CHECKOUT = 1014
        const val INTENT_DATA_TOGGLE_FULFILMENT = "INTENT_DATA_TOGGLE_FULFILMENT"
        const val INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE =
            "INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE"

        fun sendResultBack(activity: Activity?, delivery: String, needRefresh: Boolean) {
            val result = ToggleFulfilmentResult(
                needRefresh = needRefresh,
                newDeliveryType = delivery
            )
            val intent = Intent()
            intent.putExtra(INTENT_DATA_TOGGLE_FULFILMENT, result)
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
        }

        fun getIntent(
            context: Context,
            isComingFromCheckout: Boolean = false,
            isComingFromSlotSelection: Boolean = false,
            savedAddressResponse: SavedAddressResponse? = null,
            defaultAddress: Address? = null,
            whoISCollecting: String? = null,
            liquorCompliance: LiquorCompliance? = null,
        ): Intent {
            val intent = Intent(context, ShopToggleActivity::class.java)
            intent.apply {
                putExtra(BundleKeysConstants.IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
                putExtra(BundleKeysConstants.IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
                putExtra(BundleKeysConstants.SAVED_ADDRESS_RESPONSE, savedAddressResponse)
                putExtra(BundleKeysConstants.DEFAULT_ADDRESS, defaultAddress)
                if (liquorCompliance != null && liquorCompliance.isLiquorOrder && AppConfigSingleton.liquor != null && AppConfigSingleton.liquor!!.noLiquorImgUrl != null && !AppConfigSingleton.liquor!!.noLiquorImgUrl.isEmpty()) {
                    putExtra(Constant.LIQUOR_ORDER, liquorCompliance.isLiquorOrder)
                    putExtra(Constant.NO_LIQUOR_IMAGE_URL, AppConfigSingleton.liquor!!.noLiquorImgUrl)
                }
                putExtra(
                    CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
                    whoISCollecting
                )
            }
            return intent
        }
    }
    private fun sendResultBackWithUnsellableItems(
        unsellableItemsList: ArrayList<UnSellableCommerceItem>,
        deliveryType: Delivery?
    ) {
        val result =
            deliveryType?.let { ToggleFulfilmentWIthUnsellable(unsellableItemsList, it) }
        val intent = Intent()
        intent.putExtra(INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private val viewModel by viewModels<ShopToggleViewModel>()
    private var isComingFromCheckout: Boolean = false
    private var isComingFromSlotSelection: Boolean = false
    private var savedAddressResponse: SavedAddressResponse? = null
    private var whoIsCollecting: WhoIsCollectingDetails? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var autoNavigation = false
        intent?.extras?.apply {
            autoNavigation = getBoolean(BundleKeysConstants.TOGGLE_FULFILMENT_AUTO_NAVIGATION) ?: false
            isComingFromSlotSelection = getBoolean(BundleKeysConstants.IS_COMING_FROM_SLOT_SELECTION, false) ?: false
            isComingFromCheckout = getBoolean(BundleKeysConstants.IS_COMING_FROM_CHECKOUT, false) ?: false
            getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS, null)?.let {
                whoIsCollecting = Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
            }
            savedAddressResponse = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                getSerializable(BundleKeysConstants.SAVED_ADDRESS_RESPONSE, SavedAddressResponse::class.java)
            else
                getSerializable(BundleKeysConstants.SAVED_ADDRESS_RESPONSE) as? SavedAddressResponse
        }

        viewModel.setFromAutoNavigation(autoNavigation)
        val isUserAuthenticated = SessionUtilities.getInstance().isUserAuthenticated
        setContent {
            OneAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.sixteen_dp)
                    ) {

                        TopAppBar(modifier = Modifier.offset(x = (-18).dp),
                            title = { Text(text = "") },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.back24),
                                        contentDescription = stringResource(id = R.string.back_arrow)
                                    )

                                }
                            })

                        val state = viewModel.state.value
                        val confirmAddressState = viewModel.confirmAddressState.value

                        if (state.isLoading) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                CircularProgressIndicator(color = Color.Black)
                            }
                        } else {
                            ShopToggleScreen(
                                viewModel,
                                state.data,
                                isUserAuthenticated,
                                isAutoNavigated = autoNavigation
                            ) { delivery, needRefresh ->
                                if (delivery != null) {
                                    if (needRefresh) {
                                        if (delivery == Delivery.CNC) {
                                            launchStoreSelection()
                                        } else {
                                            viewModel.confirmAddress(delivery)
                                        }
                                    } else {
                                        sendResultBack(
                                            this@ShopToggleActivity,
                                            delivery = viewModel.deliveryType().type,
                                            needRefresh = false
                                        )
                                    }
                                }
                            }
                            if (confirmAddressState.isLoading) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = Color.Transparent)
                                ) {
                                    CircularProgressIndicator(color = Color.Black)
                                }
                            }

                            if (confirmAddressState.hasError) {
                                //TODO, display error message to user
                            }

                            if (confirmAddressState.isSuccess) {
                                delivery = confirmAddressState.delivery
                                if (!confirmAddressState.unsellableItems.isNullOrEmpty()) {
                                    sendResultBackWithUnsellableItems(
                                        ArrayList(confirmAddressState.unsellableItems),
                                        delivery
                                    )
                                } else {
                                    val deliveryType = KotlinUtils.getDeliveryType()?.deliveryType
                                    deliveryType?.let {
                                        if (isComingFromCheckout) {
                                            onConfirmLocationNavigation(deliveryType)
                                        } else {
                                            sendResultBack(this@ShopToggleActivity, delivery = deliveryType, needRefresh = true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchStoreSelection() {
        KotlinUtils.presentEditDeliveryGeoLocationActivity(
            this,
            REQUEST_DELIVERY_TYPE,
            Delivery.getType(KotlinUtils.getDeliveryType()?.deliveryType)
                ?: KotlinUtils.browsingDeliveryType,
            KotlinUtils.getDeliveryType()?.address?.placeId ?: "",
            isFromNewToggleFulfilmentScreen = true,
            newDelivery = Delivery.CNC,
            needStoreSelection = true,
            validateLocationResponse = viewModel.validateLocationResponse()
        )
    }

    private fun onConfirmLocationNavigation(deliveryTypeText: String) {
        if (isComingFromCheckout) {
            val delivery = Delivery.getType(deliveryTypeText)
            if (delivery == Delivery.STANDARD || delivery == Delivery.DASH) {
                if (isComingFromSlotSelection) {
                    /*Navigate to slot selection page with updated saved address*/
                    val checkoutActivityIntent =
                        Intent(
                            this,
                            CheckoutActivity::class.java
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(
                                CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                                savedAddressResponse
                            )
                            val result = when (delivery) {
                                Delivery.STANDARD -> CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION
                                else -> CheckoutAddressManagementBaseFragment.DASH_SLOT_SELECTION
                            }
                            putExtra(result, true)
                            putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
                            putExtra(
                                Constant.NO_LIQUOR_IMAGE_URL,
                                getLiquorImageUrl()
                            )
                        }
                    apply {
                        startActivityForResult(
                            checkoutActivityIntent,
                            BundleKeysConstants.FULLFILLMENT_REQUEST_CODE
                        )

                        overridePendingTransition(
                            R.anim.slide_from_right,
                            R.anim.slide_out_to_left
                        )

                    }
                    finish()
                }
            }
            else if (isComingFromSlotSelection) {
                if (whoIsCollecting != null) {
                    startCheckoutActivity(Utils.toJson(whoIsCollecting))
                } else {
                    /*Navigate to who is collecting*/
                    val placeId = KotlinUtils.getDeliveryType()?.address?.placeId
                    KotlinUtils.presentEditDeliveryGeoLocationActivity2(
                        this@ShopToggleActivity,
                        CartFragment.REQUEST_PAYMENT_STATUS,
                        GeoUtils.getDelivertyType(),
                        placeId,
                        isComingFromCheckout = true,
                        isComingFromSlotSelection = false,
                        savedAddressResponse = savedAddressResponse,
                        defaultAddress = null,
                        whoISCollecting = "",
                        isLiquorOrder = getLiquorOrder(),
                        liquorImageUrl = getLiquorImageUrl(),
                    )
                    setResult(REQUEST_DESTROY_CHECKOUT)
                    finish()
                }
            }

        } else {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun startCheckoutActivity(toJson: String?) {
        val checkoutActivityIntent = Intent(this@ShopToggleActivity, CheckoutActivity::class.java)
        checkoutActivityIntent.putExtra(
            CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
            toJson
        )
        checkoutActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        checkoutActivityIntent.putExtra(Constant.LIQUOR_ORDER, getLiquorOrder())
        checkoutActivityIntent.putExtra(Constant.NO_LIQUOR_IMAGE_URL, getLiquorImageUrl())
        startActivityForResult(checkoutActivityIntent, CartFragment.REQUEST_PAYMENT_STATUS)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_out_to_left)
        finish()
    }
    private fun getLiquorOrder(): Boolean {
        var liquorOrder = false
        intent?.extras?.apply {
            liquorOrder = getBoolean(Constant.LIQUOR_ORDER)
        }
        return liquorOrder
    }

    private fun getLiquorImageUrl(): String {
        var liquorImageUrl = ""
        intent?.extras?.apply {
            liquorImageUrl = getString(Constant.NO_LIQUOR_IMAGE_URL, "")
        }
        return liquorImageUrl
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val toggleFulfilmentResultWithUnsellable =
                getToggleFulfilmentResultWithUnSellable(data)
            if (toggleFulfilmentResultWithUnsellable != null) {
                sendResultBackWithUnsellableItems(
                    ArrayList(toggleFulfilmentResultWithUnsellable.unsellableItemsList),
                    toggleFulfilmentResultWithUnsellable.deliveryType
                )
            } else {
                if (requestCode == REQUEST_DELIVERY_TYPE) {
                    if (isComingFromCheckout) {
                        onConfirmLocationNavigation(Delivery.CNC.name)
                    } else {
                        sendResultBack()
                    }
                } else if (requestCode == BundleKeysConstants.REQUEST_CODE) {
                    sendResultBack()
                }
            }
        }
    }

    private fun unsellable(storeID:String){
        var unSellableCommerceItems: List<UnSellableCommerceItem>? = emptyList()
        WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.stores?.forEach {
            if (it.storeId==storeID) {
                unSellableCommerceItems = it.unSellableCommerceItems
                sendResultBackWithUnsellableItems(ArrayList(unSellableCommerceItems),delivery)

            }
        }
    }

    private fun sendResultBack() {
        val deliveryType = KotlinUtils.getDeliveryType()?.deliveryType
        if (!deliveryType.isNullOrEmpty()) {
            sendResultBack(this@ShopToggleActivity, delivery = deliveryType, needRefresh = true)
        }
    }


    @Parcelize
    data class ToggleFulfilmentResult(
        val needRefresh: Boolean,
        val newDeliveryType: String
    ) : Parcelable

    @Parcelize
    data class ToggleFulfilmentWIthUnsellable(

        val unsellableItemsList: List<UnSellableCommerceItem>?,
        val deliveryType: Delivery
    ) : Parcelable

    private fun getToggleFulfilmentResultWithUnSellable(intent: Intent?): ToggleFulfilmentWIthUnsellable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(
                ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE,
                ToggleFulfilmentWIthUnsellable::class.java
            )
        } else {
            intent?.extras?.getParcelable(ShopToggleActivity.INTENT_DATA_TOGGLE_FULFILMENT_UNSELLABLE)
        }
    }
}