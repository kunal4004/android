package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.order_details_activity.*
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.shop.AddOrderToCartFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.FragmentsEventsListner
import za.co.woolworths.financial.services.android.util.Utils


class OrderDetailsActivity : AppCompatActivity(), FragmentsEventsListner {

    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_details_activity)
        Utils.updateStatusBarBackground(this)
        configureUI()
    }

    companion object {
        const val TAG_ORDER_DETAILS_FRAGMENT: String = "OrderDetailsFragment"
        const val TAG_ORDER_TO_CART_FRAGMENT: String = "OrderToCartFragment"

    }

    private fun configureUI() {
        order = intent.getSerializableExtra("order") as Order?
        toolbarText.text = getString(R.string.order_page_title_prefix) + order?.orderId
        btnBack.setOnClickListener { onBackPressed() }
        replaceOrderDetailsFragment(order!!)
    }

    private fun replaceOrderDetailsFragment(order: Order) {
        replaceFragmentSafely(OrderDetailsFragment.getInstance(order), TAG_ORDER_DETAILS_FRAGMENT, false, false, R.id.fragmentContainer)
    }

    override fun onOrderItemsClicked(orderDetailsResponse: OrderDetailsResponse) {
        pushFragment(AddOrderToCartFragment.getInstance(orderDetailsResponse), TAG_ORDER_TO_CART_FRAGMENT)
    }

    fun pushFragment(fragment: Fragment, tag: String) {
        replaceFragmentSafely(fragment, tag, false, true, R.id.fragmentContainer, R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        fragment.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemsAddedToCart() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
    }
}