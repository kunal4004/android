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
import kotlinx.android.synthetic.main.other_order_details.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.cart.SubmittedOrderResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.CurrencyFormatter


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

                    setupOrderTotalDetails(response)
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
            deliveryCollectionDetailsConstraintLayout.visibility = View.VISIBLE
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

            if(response?.deliveryDetails?.deliveryInfos?.size == 2){
                oneDeliveryLinearLayout.visibility = View.GONE
                foodDeliveryLinearLayout.visibility = View.VISIBLE
                otherDeliveryLinearLayout.visibility = View.VISIBLE
                foodDeliveryDateTimeTextView.text = applyBoldBeforeComma(response
                    .deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime)
                otherDeliveryDateTimeTextView.text =
                    response.deliveryDetails?.deliveryInfos?.get(1)?.deliveryDateAndTime
            }
            else if(response?.deliveryDetails?.deliveryInfos?.size == 1){
                oneDeliveryLinearLayout.visibility = View.VISIBLE
                foodDeliveryLinearLayout.visibility = View.GONE
                otherDeliveryLinearLayout.visibility = View.GONE
                deliveryDateTimeTextView.text = applyBoldBeforeComma(response.deliveryDetails?.deliveryInfos?.get(0)?.deliveryDateAndTime)
            }
        }
    }

    private fun setupOrderTotalDetails(response: SubmittedOrderResponse?) {
        otherOrderDetailsConstraintLayout.visibility = View.VISIBLE

        orderTotalTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.total)

        yourCartTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.basketTotal)

        discountsTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.otherDiscount)

        companyDiscountTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.companyDiscount)

        wRewardsVouchersTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.voucherDiscount)

        totalDiscountTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.orderSummary?.discountDetails?.totalDiscount)

        deliveryFeeTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(response?.deliveryDetails?.shippingAmount)

        if(response?.wfsCardDetails?.isWFSCardAvailable == false) {
            if(response.orderSummary?.discountDetails?.wrewardsDiscount!! > 0.0) {
                setMissedRewardsSavings(response.orderSummary?.discountDetails?.wrewardsDiscount!!)
            }
            else if(response.orderSummary?.savedAmount!! > 10) {
                setMissedRewardsSavings(response.orderSummary?.savedAmount!!.toDouble())
            }
        }
        else{
            missedRewardsLinearLayout.visibility = View.GONE
        }

        setMissedRewardsSavings(response?.orderSummary?.discountDetails?.wrewardsDiscount!!)

    }

    private fun setMissedRewardsSavings(amount: Double) {
        missedRewardsLinearLayout.visibility = View.VISIBLE
        missedRewardsTextView.text = CurrencyFormatter
            .formatAmountToRandAndCentWithSpace(amount)
    }

    private fun applyBoldBeforeComma(deliveryDateAndTime: String?): Spannable {
        val splitDateTime = deliveryDateAndTime?.split(",", ignoreCase = false, limit = 2)
        val wordSpan: Spannable = SpannableString(deliveryDateAndTime)

        if (splitDateTime?.size == 2) {
            wordSpan.setSpan(
                StyleSpan(BOLD),
                0,
                splitDateTime[0].length + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return wordSpan
    }
}