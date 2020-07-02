package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.contact_us_financial_services.callUsLinearLayoutContainer
import kotlinx.android.synthetic.main.contact_us_detail_fragment.*
import kotlinx.android.synthetic.main.contact_us_detail_fragment.contactFinancialServicesEmailLinearLayout
import za.co.woolworths.financial.services.android.models.dto.contact_us.ContactUsOptions
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ContactUsCallCenterDetailFragment : Fragment() {

    companion object {
        private const val CONTACT_US_DETAIL = "CONTACT_US_DETAIL"
        fun newInstance(contactUsOptions: ContactUsOptions) = ContactUsCallCenterDetailFragment().apply {
            arguments = Bundle(1).apply {
                putSerializable(CONTACT_US_DETAIL, contactUsOptions)
            }
        }
    }

    private var mBottomNavigator: BottomNavigator? = null
    private var mContactUsDetail: ContactUsOptions? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mContactUsDetail = getSerializable(CONTACT_US_DETAIL) as? ContactUsOptions
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.contact_us_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        mContactUsDetail?.apply {
            value?.apply {
                call?.apply {
                    options?.forEachIndexed { index, item ->
                        val localCallerRow = layoutInflater.inflate(R.layout.contact_us_call_options_item, callUsLinearLayoutContainer, false)
                        val localCallerTextView = localCallerRow.findViewById<TextView>(R.id.localCallerTextView)
                        val localCallerPhoneNumberTextView = localCallerRow.findViewById<TextView>(R.id.localCallerPhoneNumberTextView)

                        localCallerTextView?.text = item.key
                        localCallerPhoneNumberTextView?.text = item.value
                        localCallerRow?.tag = index
                        localCallerRow?.setOnClickListener { Utils.makeCall(item.value) }

                        callUsLinearLayoutContainer?.addView(localCallerRow)
                    }

                    callCenterOperationHoursTextView?.text = operatingHours
                }

                email?.apply {
                    forEachIndexed { index, option ->
                        val localCallerRow = layoutInflater.inflate(R.layout.contact_us_email_item, contactFinancialServicesEmailLinearLayout, false)
                        val contactUsEmailTextView = localCallerRow.findViewById<TextView>(R.id.contactUsEmailTextView)
                        val contactUsEmailDescriptionTextView = localCallerRow.findViewById<TextView>(R.id.contactUsEmailDescriptionTextView)

                        val email = option.value
                        val subject = option.key

                        contactUsEmailTextView?.text = subject
                        contactUsEmailDescriptionTextView?.text = email
                        localCallerRow?.tag = index
                        localCallerRow?.setOnClickListener { KotlinUtils.sendEmail(activity, email, subject) }

                        contactFinancialServicesEmailLinearLayout?.addView(localCallerRow)
                    }
                }

                    if (faxNumber?.isNotEmpty() == true) {
                        contactUsFaxNumberRelativeLayout?.visibility = VISIBLE
                        contactUsFaxNumberHorizontalView?.visibility = VISIBLE
                        faxNumberTextView?.text = faxNumber
                    }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {}

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupToolbar()
        }
    }

    private fun setupToolbar() {
        val title = mContactUsDetail?.key ?: ""
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }
}