package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_help_and_support_fragement.*
import za.co.woolworths.financial.services.android.util.Utils

class HelpAndSupportFragment: Fragment(R.layout.layout_help_and_support_fragement) ,
    HelpAndSupportAdapter.HelpAndSupportClickListener {

    companion object {
        fun newInstance() = HelpAndSupportFragment()
    }

    var isNavigatedFromMyAccounts: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHelpAndSupportUi()
    }

    private fun setUpHelpAndSupportUi() {
        val dataList = prepareHelpAndSupportList()
        val adapter = HelpAndSupportAdapter(context, dataList, this)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        rvHelpAndSupport.setLayoutManager(llm)
        rvHelpAndSupport.setAdapter(adapter)
        imgDelBack?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    fun prepareHelpAndSupportList(): ArrayList<HelpAndSupport> {
        /* prepare data list as per delivery type , currently done for standard and CNC only*/
        val dataList = arrayListOf<HelpAndSupport>()
        dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care),
            getString(R.string.dash_customer_care_no), R.drawable.help_phone))
        dataList.add(HelpAndSupport(getString(R.string.cancel_order),
            "", R.drawable.ic_dash_cancel_order))
        dataList.add(HelpAndSupport(getString(R.string.dash_send_us_an_email),
                getString(R.string.dash_email_id), R.drawable.ic_envelope))
        dataList.add(HelpAndSupport(getString(R.string.dash_Chat_to_your_shopper), "", R.drawable.ic_dash_chat_support_icon))
        dataList.add(HelpAndSupport(getString(R.string.dash_track_your_order),
                "", R.drawable.ic_dash_track_order_icon))
        dataList.add(HelpAndSupport(getString(R.string.view_tax_invoice),
                "", R.drawable.ic_tax_invoice))
        return dataList
    }

    override fun openCallSupport(contactNumber: String) {
        Utils.makeCall(contactNumber)
    }

    override fun onCancelOrder() {
       /* activity?.apply {
            this@HelpAndSupportFragment.childFragmentManager.apply {
                CancelOrderConfirmationDialogFragment.newInstance(isNavigatedFromMyAccounts)
                        .show(this, CancelOrderConfirmationDialogFragment::class.java.simpleName)
            }
        }*/
    }

    override fun onCancelOrderConfirmation() {
        /*activity?.apply {
            val intent = Intent(this, CancelOrderProgressActivity::class.java)
            intent.putExtra(CancelOrderProgressFragment.ORDER_ID, order?.orderId)
            intent.putExtra(AppConstant.NAVIGATED_FROM_MY_ACCOUNTS, isNavigatedFromMyAccounts)
            startActivityForResult(intent, CancelOrderProgressFragment.REQUEST_CODE_CANCEL_ORDER)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }*/
    }

    override fun openEmailSupport(emailId: String) {
       // Utils.makeCall(emailId)
      //  Utils.sendEmail(url, "", getApplicationContext())


        //val emails_in_to = arrayOf("to@email.com")
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            data = Uri.parse("shop@wooliesdash.co.za")
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, "shop@wooliesdash.co.za")
            putExtra(Intent.EXTRA_SUBJECT, "Dash Order:O304950160")
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
          //  intent.setPackage("com.google.android.gm")
            startActivity(intent)
        } else {
           // Log.d(TAG, "No app available to send email.")
        }

        /*val email = "shop@wooliesdash.co.za"
        val subject = "Dash Order:O304960007"
                KotlinUtils.sendEmail(activity, email, subject)*/



    }

    override fun openChatSupport() {
      //  TODO("Not yet implemented")
    }

    override fun openTrackYourOrder() {
       // TODO("Not yet implemented")
    }

    override fun openTaxInvoice() {
       // TODO("Not yet implemented")
    }

}