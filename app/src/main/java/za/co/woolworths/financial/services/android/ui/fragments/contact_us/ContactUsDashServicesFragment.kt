package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ContactUsCustomerServiceBinding
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class ContactUsDashServicesFragment : Fragment(R.layout.contact_us_customer_service) {

    private lateinit var binding: ContactUsCustomerServiceBinding
    private val customerServicesModel = ContactUsModel()

    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ContactUsCustomerServiceBinding.bind(view)

        setupToolbar()

        customerServicesModel.contactUsDashServicesOptions()?.forEachIndexed { index, contactUsOptions ->
            val customerServiceItem = layoutInflater.inflate(R.layout.contact_us_customer_services_landing_item, binding.customerServicesLinearLayout, false)
            val customerServicesTextView = customerServiceItem.findViewById<TextView>(R.id.customerServicesTextView)
            val customerServicesDescriptionTextView = customerServiceItem.findViewById<TextView>(R.id.customerServicesDescriptionTextView)
            val customerServicesDivider = customerServiceItem.findViewById<View>(R.id.customerServicesDivider)

            customerServicesTextView?.text = contactUsOptions.key
            when(contactUsOptions.description.isNullOrEmpty()){
                true->{
                    customerServicesDescriptionTextView?.visibility = GONE

                }
                false->{
                    customerServicesDescriptionTextView?.text = contactUsOptions.description
                }
            }
            customerServiceItem?.tag = index
            if (index == customerServicesModel.contactUsDashServicesOptions()!!.size.minus(2)){
                customerServicesDivider.visibility = View.GONE
            }
            customerServiceItem?.setOnClickListener { openFragment(ContactUsCallCenterDetailFragment.newInstance(contactUsOptions)) }
            binding.customerServicesLinearLayout?.addView(customerServiceItem)
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
        val title = customerServicesModel.contactUsDashServices()?.title
        mBottomNavigator?.apply {
            setTitle(title)
            displayToolbar()
            showBackNavigationIcon(true)
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(title)
    }
}