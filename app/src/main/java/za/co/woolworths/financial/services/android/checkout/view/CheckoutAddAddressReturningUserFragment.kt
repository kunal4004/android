package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_native_checkout_delivery_order_summary.*
import za.co.woolworths.financial.services.android.models.dto.OrderSummary
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.CurrencyFormatter.Companion.formatAmountToRandAndCentWithSpace

/**
 * Created by Kunal Uttarwar on 27/05/21.
 */
class CheckoutAddAddressReturningUserFragment : Fragment() {

    companion object {
        const val KEY_ARGS_ORDER_SUMMARY = "ORDER_SUMMARY"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_add_address_retuning_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initOrderSummary()
    }

    /**
     * Initializes Order Summary data from argument.
     */
    private fun initOrderSummary() {
        arguments?.apply {
            val orderSummary = getParcelable(KEY_ARGS_ORDER_SUMMARY) as? OrderSummary
            orderSummary?.let { orderSummary ->
                txtOrderSummaryYourCartValue?.text =
                    CurrencyFormatter.formatAmountToRandAndCentWithSpace(orderSummary.basketTotal)
                orderSummary.discountDetails?.let { discountDetails ->
                    txtOrderSummaryDiscountValue?.text =
                        "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.otherDiscount)
                    txtOrderSummaryTotalDiscountValue?.text =
                        "-" + CurrencyFormatter.formatAmountToRandAndCentWithSpace(discountDetails.totalDiscount)
                }
            }
        }
    }
}
