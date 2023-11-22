package za.co.woolworths.financial.services.android.shoptoggle.presentation

import android.app.Activity
import android.content.Intent
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.shoptoggle.presentation.components.ShopToggleScreen
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

@AndroidEntryPoint
class ShopToggleActivity : ComponentActivity() {

    companion object {
        const val REQUEST_DELIVERY_TYPE = 105
        const val INTENT_DATA_TOGGLE_FULFILMENT = "INTENT_DATA_TOGGLE_FULFILMENT"

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
    }

    private val viewModel by viewModels<ShopToggleViewModel>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                            ShopToggleScreen(viewModel, state.data) { delivery ->
                                if (delivery != null) {
                                    if (delivery == Delivery.CNC) {
                                        launchStoreSelection()
                                    } else {
                                        viewModel.confirmAddress(delivery)
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
                                if (!confirmAddressState.unsellableItems.isNullOrEmpty()) {
                                    //TODO, navigate back to the previous page & display unsellable dialog
                                } else {
                                    val deliveryType = KotlinUtils.getDeliveryType()?.deliveryType
                                    deliveryType?.let {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_DELIVERY_TYPE || requestCode == BundleKeysConstants.REQUEST_CODE) {
                sendResultBack()
            }
        }
    }

    private fun sendResultBack() {
        val deliveryType = KotlinUtils.getDeliveryType()?.deliveryType
        if (!deliveryType.isNullOrEmpty()) {
            sendResultBack(this@ShopToggleActivity, delivery = deliveryType, needRefresh = true)
        }
    }
}

@Parcelize
data class ToggleFulfilmentResult(
    val needRefresh: Boolean,
    val newDeliveryType: String
): Parcelable