package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutHelpAndSupportFragementBinding
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.OrderDetailsResponse
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.ui.activities.CancelOrderProgressActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderConfirmationDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.CancelOrderProgressFragment
import za.co.woolworths.financial.services.android.ui.fragments.shop.OrderTrackingWebViewActivity
import za.co.woolworths.financial.services.android.ui.fragments.shop.TaxInvoiceLIstFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class HelpAndSupportFragment : BaseFragmentBinding<LayoutHelpAndSupportFragementBinding>(LayoutHelpAndSupportFragementBinding::inflate), CancelOrderConfirmationDialogFragment.ICancelOrderConfirmation,
        HelpAndSupportAdapter.HelpAndSupportClickListener {

    private var orderDetailsResponse: OrderDetailsResponse? = null
    var isNavigatedFromMyAccounts: Boolean = false
    private var orderItemList: ArrayList<CommerceItem>? = null

    companion object {
        const val STORE_CARD_DETAIL = "STORE_CARD_DETAIL"
        const val KEY_ARGS_ORDER_STATUS = "orderStatusResponse"

        fun newInstance(orderDetailsResponse: OrderDetailsResponse?, orderItemList: ArrayList<CommerceItem>) = HelpAndSupportFragment().withArgs {
            putSerializable(KEY_ARGS_ORDER_STATUS, orderDetailsResponse)
            putSerializable(AppConstant.ORDER_ITEM_LIST, orderItemList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHelpAndSupportUi()
    }

    private fun setUpHelpAndSupportUi() {
         arguments?.let {
             orderDetailsResponse = it.getSerializable(KEY_ARGS_ORDER_STATUS) as? OrderDetailsResponse
             orderItemList = arguments?.getSerializable(AppConstant.ORDER_ITEM_LIST) as? ArrayList<CommerceItem>?
        }
        val dataList = prepareHelpAndSupportList(orderDetailsResponse)
        val adapter = HelpAndSupportAdapter(context, dataList, this)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL

        binding.apply {
            rvHelpAndSupport.layoutManager = llm
            rvHelpAndSupport.adapter = adapter
            imgDelBack.setOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    fun prepareHelpAndSupportList(orderDetailsResponse: OrderDetailsResponse?): ArrayList<HelpAndSupport> {
        /* prepare data list as per delivery type , currently done for standard and CNC only*/
        val dataList = arrayListOf<HelpAndSupport>()
        dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care), getString(R.string.dash_customer_care_no_phone), R.drawable.help_phone))
        dataList.add(HelpAndSupport(getString(R.string.dash_send_us_an_email), getString(R.string.dash_email_id), R.drawable.ic_envelope))
        orderDetailsResponse?.orderSummary?.apply {
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
                    intent.putExtra(AppConstant.ORDER_ITEM_LIST, orderItemList)
                    intent.putExtra(AppConstant.ORDER_ITEM_TOTAL, orderDetailsResponse?.orderSummary?.total)
                    intent.putExtra(AppConstant.ORDER_SHIPPING_TOTAL, orderDetailsResponse?.orderSummary?.estimatedDelivery)

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
        orderDetailsResponse?.orderSummary?.orderId?.let { orderId ->
            startActivity(OCChatActivity.newIntent(requireActivity(), orderId))
        }
    }

    override fun openTrackYourOrder() {
        orderDetailsResponse?.orderSummary?.driverTrackingURL?.let { driverTrackingURL->
            activity?.apply {startActivity(OrderTrackingWebViewActivity.newIntent(requireActivity(), driverTrackingURL))
                overridePendingTransition(
                    R.anim.slide_from_right,
                    R.anim.slide_out_to_left
                )
            }
        }
    }
}