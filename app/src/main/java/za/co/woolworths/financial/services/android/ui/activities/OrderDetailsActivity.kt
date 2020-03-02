package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.order_details_activity.*
import za.co.woolworths.financial.services.android.contracts.IToastInterface
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.Order
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.PDP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.replaceFragmentSafely
import za.co.woolworths.financial.services.android.ui.fragments.shop.AddOrderToCartFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment.Companion.RESULT_CODE_CANCEL_ORDER_SUCCESS
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderDetailsFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.TaxInvoiceLIstFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.FragmentsEventsListner
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.views.ToastFactory
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class OrderDetailsActivity : AppCompatActivity(), FragmentsEventsListner, IToastInterface {

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
        const val TAG_TAX_INVOICE_FRAGMENT: String = "TaxInvoiceFragment"
        const val ORDER_ID: String = "ORDER_ID"
        const val REQUEST_CODE_ORDER_DETAILS_PAGE = 1989
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
        if(requestCode == CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER && resultCode == RESULT_CODE_CANCEL_ORDER_SUCCESS){
            setResult(RESULT_CODE_CANCEL_ORDER_SUCCESS)
            finish()
            return
        }

        if (requestCode == ADD_TO_SHOPPING_LIST_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            ToastFactory.buildShoppingListToast(this,fragmentContainer, true, data, this)
            return
        }
        if (requestCode == PDP_REQUEST_CODE && resultCode == ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE) {
            setResult(ADD_TO_SHOPPING_LIST_FROM_PRODUCT_DETAIL_RESULT_CODE, data)
            finish()
            overridePendingTransition(0, 0)
            return
        }
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        fragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onItemsAddedToCart(addItemToCartResponse: AddItemToCartResponse) {
        when (addItemToCartResponse.httpCode) {
            200 -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
                ToastFactory.buildAddToCartSuccessToast(fragmentContainer, true, this)
            }
            440 -> {
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, addItemToCartResponse.response.stsParams, this)
                finish()
            }

        }
    }

    override fun openTaxInvoices() {
        pushFragment(TaxInvoiceLIstFragment.getInstance(order?.orderId!!, order?.taxNoteNumbers!!), TAG_TAX_INVOICE_FRAGMENT)
    }

    override fun onToastButtonClicked(jsonElement: JsonElement?) {
        jsonElement?.let { NavigateToShoppingList.navigateToShoppingListOnToastClicked(this, it) }
    }
}