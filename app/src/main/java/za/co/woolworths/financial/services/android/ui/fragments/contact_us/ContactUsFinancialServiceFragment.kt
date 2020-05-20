package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.contact_us_financial_services.*
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppConfig
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class ContactUsFinancialServiceFragment : Fragment(), View.OnClickListener {

    companion object {
        private const val REQUEST_CALL = 1

    }

    private var callIntent: Intent? = null
    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.contact_us_financial_services, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        showWhatsAppChatWithUs()

        localCaller?.setOnClickListener(this)
        internationalCaller?.setOnClickListener(this)
        blackCrediCardQuery?.setOnClickListener(this)
        complains?.setOnClickListener(this)
        creditCardQuery?.setOnClickListener(this)
        wRewardsQuery?.setOnClickListener(this)
        insuranceClaim?.setOnClickListener(this)
        paymentQuery?.setOnClickListener(this)
        storeCardPesonalLoanQuery?.setOnClickListener(this)
        proofOfIncome?.setOnClickListener(this)
        technical?.setOnClickListener(this)
        contactUsChatToUsRelativeLayout?.setOnClickListener(this)
    }

    private fun showWhatsAppChatWithUs() {
        val chatWithUsIsEnabled = WhatsAppConfig().contactUsFinancialServicesIsEnabled
        chatWithUsLinearLayout?.visibility = if (chatWithUsIsEnabled) VISIBLE else GONE
    }

    override fun onClick(v: View) {
        activity?.resources?.apply {
            when (v.id) {
                R.id.localCaller -> Utils.makeCall(getString(R.string.fs_local_caller_number))
                R.id.internationalCaller -> Utils.makeCall(getString(R.string.fs_inter_national_caller_number))
                R.id.blackCrediCardQuery -> sendEmail(getString(R.string.email_black_credit_card_query), getString(R.string.txt_black_credit_card_query))
                R.id.complains -> sendEmail(getString(R.string.email_complaints), getString(R.string.txt_complaint))
                R.id.creditCardQuery -> sendEmail(getString(R.string.email_credicard_query), getString(R.string.txt_cc_query))
                R.id.wRewardsQuery -> sendEmail(getString(R.string.email_wrewards_query), getString(R.string.txt_wrewards_query))
                R.id.insuranceClaim -> sendEmail(getString(R.string.email_insurance_claim), getString(R.string.txt_insurance_claim))
                R.id.paymentQuery -> sendEmail(getString(R.string.email_payment_query), getString(R.string.txt_payment_query))
                R.id.storeCardPesonalLoanQuery -> sendEmail(getString(R.string.email_sc_and_pl_query), getString(R.string.txt_sc_and_pl_query))
                R.id.proofOfIncome -> sendEmail(getString(R.string.email_proof_of_income), getString(R.string.txt_proof_of_income))
                R.id.technical -> sendEmail(getString(R.string.email_technical), getString(R.string.txt_technical_problem))
                R.id.contactUsChatToUsRelativeLayout -> ScreenManager.presentWhatsAppChatToUsActivity(activity)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CALL -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(callIntent)
            }
        }
    }

    fun sendEmail(emailId: String, subject: String?) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:" + emailId +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(""))
        val listOfEmail =
                activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
        if (listOfEmail.size > 0) {
            startActivity(emailIntent)
        } else {
            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.INFO, activity?.resources?.getString(R.string.contact_us_no_email_error)?.replace("email_address", emailId)?.replace("subject_line", subject
                    ?: ""))
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(activity?.resources?.getString(R.string.contact_us_financial_services))
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.contact_us_financial_services))

    }
}