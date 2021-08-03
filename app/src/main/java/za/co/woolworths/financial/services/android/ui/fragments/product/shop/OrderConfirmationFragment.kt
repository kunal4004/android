package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.graphics.Typeface.BOLD
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delivering_to_collection_from.*
import kotlinx.android.synthetic.main.fragment_order_confirmation.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString


class OrderConfirmationFragment : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_order_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOrderDetails()
    }

    private fun getOrderDetails() {
        OneAppService.getSubmittedOrder()
            .enqueue(CompletionHandler(object : IResponseListener<SubmittedOrderResponse> {
                override fun onSuccess(response: SubmittedOrderResponse?) {
                    response?.orderSummary?.orderId?.let { setToolbar(it) }

                    setupDeliveryOrCollectionDetails(response)
                }

                override fun onFailure(error: Throwable?) {
                    //TODO: handle error
                }
            }, SubmittedOrderResponse::class.java))
    }

    private fun setToolbar(orderId: String) {
        orderIdTextView.text = bindString(R.string.order_details_toolbar_title, orderId)
    }

    private fun setupDeliveryOrCollectionDetails(response: SubmittedOrderResponse?) {
        context?.let {
            deliveryCollectionDetails.visibility = View.VISIBLE
            if (response?.orderSummary?.store?.name != null) {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_collection_grey_bg)
                optionTitle.text = it.getText(R.string.collecting_from)
                optionLocation.text = response.orderSummary?.store?.name

            } else {
                optionImage.background =
                    AppCompatResources.getDrawable(it, R.drawable.icon_delivery_grey_bg)
                optionTitle.text = it.getText(R.string.delivering_to)
                optionLocation.text = response?.deliveryDetails?.shippingAddress?.address1
            }

            if(response?.deliveryDetails?.deliveryInfos?.size!! >= 1){
                val splitDateTime = response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime?.split(",", ignoreCase = false, limit = 2)

                if(splitDateTime?.size == 2){
                    val wordSpan: Spannable =
                        SpannableString(response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime)

                    wordSpan.setSpan(
                        StyleSpan(BOLD),
                        0,
                        splitDateTime[0].length+1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    deliveryDateTime.text = wordSpan
                }
                else{
                    deliveryDateTime.text =
                        response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime
                }
            }
        }
    }
}