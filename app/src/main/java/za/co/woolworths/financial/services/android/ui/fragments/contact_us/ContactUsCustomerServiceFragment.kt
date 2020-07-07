package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.contact_us_customer_service.*
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class ContactUsCustomerServiceFragment : Fragment() {

    private val customerServicesModel = ContactUsModel()

    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.contact_us_customer_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        customerServicesModel.contactUsCustomerServicesOptions()?.forEachIndexed { index, contactUsOptions ->
            val customerServiceItem = layoutInflater.inflate(R.layout.contact_us_customer_services_landing_item, customerServicesLinearLayout, false)
            val customerServicesTextView = customerServiceItem.findViewById<TextView>(R.id.customerServicesTextView)
            val customerServicesDescriptionTextView = customerServiceItem.findViewById<TextView>(R.id.customerServicesDescriptionTextView)

            customerServicesTextView?.text = contactUsOptions.key
            customerServicesDescriptionTextView?.text = contactUsOptions.description
            customerServiceItem?.tag = index

            customerServiceItem?.setOnClickListener { openFragment(ContactUsCallCenterDetailFragment.newInstance(contactUsOptions)) }
            customerServicesLinearLayout?.addView(customerServiceItem)
        }
    }

    private fun openFragment(fragment: Fragment?) {
        if (activity is BottomNavigationActivity) {
            mBottomNavigator?.pushFragment(fragment)
        } else {
            fragment?.let { frag -> (activity as? MyAccountActivity)?.replaceFragment(frag) }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        val title = customerServicesModel.contactUsCustomerServices()?.title
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }
}