package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ContactUsFinancialServicesBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.Companion.CONTACT_US
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs.Companion.FEATURE_WHATSAPP
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils

class ContactUsFinancialServiceFragment : Fragment(R.layout.contact_us_financial_services), View.OnClickListener {

    private lateinit var binding: ContactUsFinancialServicesBinding
    private val contactUsModel = ContactUsModel()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ContactUsFinancialServicesBinding.bind(view)

        binding.apply {
            setupToolbar()
            with(contactUsModel) {
                contactUsFinancialServicesEmail()?.apply {
                    val localCallerRow = layoutInflater.inflate(
                        R.layout.contact_us_email_item,
                        contactFinancialServicesEmailLinearLayout,
                        false
                    )
                    val contactUsEmailTextView =
                        localCallerRow.findViewById<TextView>(R.id.contactUsEmailTextView)
                    val contactUsEmailDescriptionTextView =
                        localCallerRow.findViewById<TextView>(R.id.contactUsEmailDescriptionTextView)
                    val contactUsEmailDescriptionDivider =
                        localCallerRow.findViewById<View>(R.id.contactUsEmailDescriptionDivider)

                    contactUsEmailTextView?.text = bindString(R.string.send_enquiry)
                    contactUsEmailDescriptionTextView?.visibility = GONE
                    contactUsEmailDescriptionDivider?.visibility = GONE
                    localCallerRow?.setOnClickListener {
                        if (activity is BottomNavigationActivity)
                            mBottomNavigator?.pushFragmentSlideUp(EnquiriesListFragment())
                        else
                            (activity as? MyAccountActivity)?.replaceFragment(EnquiriesListFragment())
                    }

                    contactFinancialServicesEmailLinearLayout?.addView(localCallerRow)

                }

                contactUsFinancialServicesCall()?.apply {
                    options?.forEachIndexed { index, item ->
                        val localCallerRow = layoutInflater.inflate(
                            R.layout.contact_us_call_options_item,
                            callUsLinearLayoutContainer,
                            false
                        )
                        val localCallerTextView =
                            localCallerRow.findViewById<TextView>(R.id.localCallerTextView)
                        val localCallerPhoneNumberTextView =
                            localCallerRow.findViewById<TextView>(R.id.localCallerPhoneNumberTextView)

                        localCallerTextView?.text = item.key
                        localCallerPhoneNumberTextView?.text = item.value
                        localCallerRow?.tag = index
                        localCallerRow?.setOnClickListener { Utils.makeCall(item.value) }

                        callUsLinearLayoutContainer?.addView(localCallerRow)
                    }

                    operationHoursTextView?.text = operatingHours
                }
            }

            showWhatsAppChatWithUs()
            contactUsChatToUsRelativeLayout?.setOnClickListener(this@ContactUsFinancialServiceFragment)
        }
    }

    private fun ContactUsFinancialServicesBinding.showWhatsAppChatWithUs() {
        with(WhatsAppChatToUs()) {
            if (isChatWithUsEnabledForContactUs) {
                chatWithUsLinearLayout?.visibility = VISIBLE
                if (isCustomerServiceAvailable) {
                    whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp_grey)
                    whatsAppTitleTextView?.setTextColor(Color.BLACK)
                    whatsAppNextIconImageView?.alpha = 1f
                } else {
                    whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp_grey)
                    whatsAppNextIconImageView?.alpha = 0.4f
                    activity?.let { activity -> whatsAppTitleTextView?.setTextColor(ContextCompat.getColor(activity, R.color.unavailable)) }
                }
            } else {
                chatWithUsLinearLayout?.visibility = GONE
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.contactUsChatToUsRelativeLayout -> {
                if (!WhatsAppChatToUs().isCustomerServiceAvailable) {
                    val whatsAppUnavailableFragment = WhatsAppUnavailableFragment()
                    activity?.supportFragmentManager?.let { supportFragmentManager -> whatsAppUnavailableFragment.show(supportFragmentManager, WhatsAppUnavailableFragment::class.java.simpleName) }
                    return
                }
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_CONTACT_US, this) }
                ScreenManager.presentWhatsAppChatToUsActivity(activity, FEATURE_WHATSAPP, CONTACT_US)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CALL -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activity?.startActivity(callIntent)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        val title = contactUsModel.contactUsFinancialService()?.title
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }
}