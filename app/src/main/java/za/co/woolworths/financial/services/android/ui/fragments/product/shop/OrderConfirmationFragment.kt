package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
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
                }

                override fun onFailure(error: Throwable?) {
                    //TODO: handle error
                }
            }, SubmittedOrderResponse::class.java))
    }

    private fun setToolbar(orderId: String) {
        orderIdTextView.text = bindString(R.string.order_details_toolbar_title, orderId)
    }

}