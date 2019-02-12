package za.co.woolworths.financial.services.android.ui.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.models.rest.product.GetOrderDetailsRequest
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.Utils

class OrderDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.order_details_activity)
        Utils.updateStatusBarBackground(this)
        initViews()
    }

    private fun initViews() {
        requestOrderDetails("").execute()
    }

    fun requestOrderDetails(orderId: String): GetOrderDetailsRequest {
        return GetOrderDetailsRequest(this, orderId, object : OnEventListener<OrderDetailsResponse> {
            override fun onSuccess(ordersResponse: OrderDetailsResponse) {
                    bindData(ordersResponse)
            }

            override fun onFailure(e: String?) {

            }

        })

    }

    private fun bindData(ordersResponse: OrderDetailsResponse) {
            
    }
}