package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ContactUsDetailFragmentBinding
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUsOptions
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

class ContactUsCallCenterDetailFragment : Fragment(R.layout.contact_us_detail_fragment) {

    companion object {
        private const val CONTACT_US_DETAIL = "CONTACT_US_DETAIL"
        fun newInstance(contactUsOptions: ConfigContactUsOptions) = ContactUsCallCenterDetailFragment().apply {
            arguments = Bundle(1).apply {
                putSerializable(CONTACT_US_DETAIL, contactUsOptions)
            }
        }
    }

    private lateinit var binding: ContactUsDetailFragmentBinding
    private var mBottomNavigator: BottomNavigator? = null
    private var mContactUsDetail: ConfigContactUsOptions? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mContactUsDetail = getSerializable(CONTACT_US_DETAIL) as? ConfigContactUsOptions
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ContactUsDetailFragmentBinding.bind(view)

        setupToolbar()

        mContactUsDetail?.apply {
            value?.apply {
                call?.apply {
                    options?.forEachIndexed { index, item ->
                        val localCallerRow = layoutInflater.inflate(R.layout.contact_us_call_options_item, binding.callUsLinearLayoutContainer, false)
                        val localCallerTextView = localCallerRow.findViewById<TextView>(R.id.localCallerTextView)
                        val localCallerPhoneNumberTextView = localCallerRow.findViewById<TextView>(R.id.localCallerPhoneNumberTextView)
                        val localCallerPhoneNumberDivider = localCallerRow.findViewById<View>(R.id.localCallerPhoneNumberDivider)
                        val localCallerPhoneNumberImageView = localCallerRow.findViewById<ImageView>(R.id.localCallerImg)
                        localCallerPhoneNumberImageView.setColorFilter(resources.getColor(R.color.color_444444), PorterDuff.Mode.SRC_IN)


                        localCallerTextView?.text = item.key
                        localCallerPhoneNumberTextView?.text = item.value
                        localCallerRow?.tag = index
                        localCallerRow?.setOnClickListener { Utils.makeCall(item.value) }
                        if (index == options.size.minus(1)){
                            localCallerPhoneNumberDivider.visibility = GONE
                        }
                        binding.callUsLinearLayoutContainer?.addView(localCallerRow)
                    }

                    binding.callCenterOperationHoursTextView?.text = operatingHours
                }

                email?.apply {
                    forEachIndexed { index, option ->
                        val localCallerRow = layoutInflater.inflate(R.layout.contact_us_email_item, binding.contactFinancialServicesEmailLinearLayout, false)
                        val contactUsEmailTextView = localCallerRow.findViewById<TextView>(R.id.contactUsEmailTextView)
                        val contactUsEmailDescriptionTextView = localCallerRow.findViewById<TextView>(R.id.contactUsEmailDescriptionTextView)
                        val contactUsEmailDescriptionDivider = localCallerRow.findViewById<View>(R.id.contactUsEmailDescriptionDivider)

                        val email = option.value
                        val subject = option.key

                        contactUsEmailTextView?.text = subject
                        contactUsEmailDescriptionTextView?.text = email
                        localCallerRow?.tag = index
                        localCallerRow?.setOnClickListener { KotlinUtils.sendEmail(activity, email, subject) }

                        if (index == this.size.minus(1)){
                            contactUsEmailDescriptionDivider.visibility = GONE
                        }
                        binding.contactFinancialServicesEmailLinearLayout?.addView(localCallerRow)
                    }
                }

                    if (faxNumber?.isNotEmpty() == true) {
                        binding.contactUsFaxNumberRelativeLayout?.visibility = VISIBLE
                        binding.contactUsFaxNumberHorizontalView?.visibility = VISIBLE
                        binding.faxNumberTextView?.text = faxNumber
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