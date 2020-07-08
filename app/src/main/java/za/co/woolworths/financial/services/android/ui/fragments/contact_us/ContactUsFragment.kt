package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_contact_us.*
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.extension.bindString


class ContactUsFragment : Fragment(), View.OnClickListener {

    private var mBottomNavigator: BottomNavigator? = null
    private val contactUsModel = ContactUsModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity) {
            mBottomNavigator = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MyAccountActivity?)?.supportActionBar?.show()
        setupToolbar()

        with(contactUsModel) {
            contactUsLanding()?.forEachIndexed { index, item ->

                val contactUsRow = layoutInflater.inflate(R.layout.contact_us_landing_item, contactUsContainerLinearLayout, false)
                val contactUsLandingTextView = contactUsRow.findViewById<TextView>(R.id.contactUsLandingTextView)
                val contactUsLandingDescriptionTextView = contactUsRow.findViewById<TextView>(R.id.contactUsLandingDescriptionTextView)

                contactUsLandingTextView?.text = item.title
                contactUsLandingDescriptionTextView?.text = item.description
                contactUsRow?.tag = index
                contactUsRow?.setOnClickListener (this@ContactUsFragment)

                contactUsContainerLinearLayout?.addView(contactUsRow)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.tag as? Int) {
            0 -> {
                if (activity is BottomNavigationActivity)
                    mBottomNavigator?.pushFragment(ContactUsFinancialServiceFragment())
                else
                    (activity as? MyAccountActivity)?.replaceFragment(ContactUsFinancialServiceFragment())
            }
            1 -> {
                if (activity is BottomNavigationActivity)
                    mBottomNavigator?.pushFragment(ContactUsCustomerServiceFragment())
                else
                    (activity as? MyAccountActivity)?.replaceFragment(ContactUsCustomerServiceFragment())
            }
        }
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(bindString(R.string.contact_us))
            showBackNavigationIcon(true)
            (activity as? BottomNavigationActivity)?.apply {
                setToolbarBackgroundDrawable(R.drawable.appbar_background)
                showToolbar()
            }
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(bindString(R.string.contact_us))

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }
}