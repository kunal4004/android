package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_help_and_support_fragement.*
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderConfirmationDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.TaxInvoiceLIstFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery

class HelpAndSupportFragment : Fragment(R.layout.layout_help_and_support_fragement), CancelOrderConfirmationDialogFragment.ICancelOrderConfirmation,
        HelpAndSupportAdapter.HelpAndSupportClickListener {

    var orderDetailsResponse: OrderDetailsResponse? = null
    var isNavigatedFromMyAccounts: Boolean = false

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
        const val KEY_ARGS_ORDER_STATUS = "orderStatusResponse"

        fun newInstance(orderDetailsResponse: OrderDetailsResponse?) = HelpAndSupportFragment().withArgs {
            putSerializable(KEY_ARGS_ORDER_STATUS, orderDetailsResponse)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHelpAndSupportUi()
    }

    private fun setUpHelpAndSupportUi() {
        orderDetailsResponse = arguments?.let {
            it.getSerializable(KEY_ARGS_ORDER_STATUS) as? OrderDetailsResponse
        }
        val dataList = prepareHelpAndSupportList(orderDetailsResponse)
        val adapter = HelpAndSupportAdapter(context, dataList, this)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        rvHelpAndSupport.setLayoutManager(llm)
        rvHelpAndSupport.setAdapter(adapter)
        imgDelBack?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    fun prepareHelpAndSupportList(orderDetailsResponse: OrderDetailsResponse?): ArrayList<HelpAndSupport> {
        /* prepare data list as per delivery type , currently done for standard and CNC only*/
        val dataList = arrayListOf<HelpAndSupport>()
        orderDetailsResponse?.orderSummary?.apply {
            val delivery: String? = fulfillmentDetails?.deliveryType
            when (Delivery.getType(delivery)) {
                Delivery.STANDARD -> {
                    dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care), getString(R.string.dash_customer_care_no), R.drawable.help_phone))
                    dataList.add(HelpAndSupport(getString(R.string.dash_send_us_an_email), getString(R.string.email_online_shop), R.drawable.ic_envelope))
                }
                Delivery.CNC -> {
                    dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care), getString(R.string.dash_customer_care_no), R.drawable.help_phone))
                    dataList.add(HelpAndSupport(getString(R.string.dash_send_us_an_email), getString(R.string.email_online_shop), R.drawable.ic_envelope))
                }
                Delivery.DASH -> {
                    dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care), getString(R.string.dash_customer_care_no_phone), R.drawable.help_phone))
                    dataList.add(HelpAndSupport(getString(R.string.dash_send_us_an_email), getString(R.string.dash_email_id), R.drawable.ic_envelope))
                }
            }

            if (orderCancellable && !requestCancellation)
                dataList.add(HelpAndSupport(getString(R.string.cancel_order), "", R.drawable.ic_dash_cancel_order))

            if (isChatEnabled)
                dataList.add(HelpAndSupport(getString(R.string.dash_Chat_to_your_shopper) + " " + shopperName, "", R.drawable.ic_dash_chat_support_icon))

            if (isDriverTrackingEnabled)
                dataList.add(HelpAndSupport(getString(R.string.dash_track_your_order), "", R.drawable.ic_dash_track_order_icon))

            if (!taxNoteNumbers.isNullOrEmpty())
                dataList.add(HelpAndSupport(getString(R.string.view_tax_invoice), "", R.drawable.ic_tax_invoice))
        }
        return dataList
    }

    override fun openCallSupport(contactNumber: String) {
        Utils.makeCall(contactNumber)
    }

    override fun onCancelOrder() {
        requireActivity().apply {
            this@HelpAndSupportFragment.childFragmentManager.apply {
                CancelOrderConfirmationDialogFragment.newInstance(isNavigatedFromMyAccounts)
                        .show(this, CancelOrderConfirmationDialogFragment::class.java.simpleName)
            }
        }
    }

    override fun onCancelOrderConfirmation() {
        requireActivity().apply {
            val intent = Intent(this, CancelOrderProgressActivity::class.java)
            orderDetailsResponse?.orderSummary?.let {
                it.orderId?.let { _ ->
                    intent.putExtra(CancelOrderProgressFragment.ORDER_ID, it.orderId)
                    intent.putExtra(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccounts)
                    startActivityForResult(intent, CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER)
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }
        }
    }

    override fun openEmailSupport(emailId: String) {
        val email = getString(R.string.dash_email_id)
        val orderId = orderDetailsResponse?.orderSummary?.orderId
        val subject = getString(R.string.dash_order) + orderId
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = getString(R.string.dash_email_type)
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        this.startActivity(emailIntent)
    }

    override fun openTaxInvoice() {
        (requireActivity() as? BottomNavigationActivity)?.pushFragment(
                orderDetailsResponse?.orderSummary?.let {
                    it.orderId?.let { itData ->
                        TaxInvoiceLIstFragment.getInstance(
                                itData, it.taxNoteNumbers ?: ArrayList(0)
                        )
                    }
                }
        )
    }

    override fun openChatSupport() {
        //  TODO("Not yet implemented")
    }

    override fun openTrackYourOrder() {
        // TODO("Not yet implemented")
    }
}