package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_contact_us.*
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class ContactUsFragment : Fragment(), View.OnClickListener {

    private var mBottomNavigator: BottomNavigator? = null

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
        financialService?.setOnClickListener(this)
        customerService?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.financialService -> {
                if (activity is BottomNavigationActivity)
                    mBottomNavigator?.pushFragment(ContactUsFinancialServiceFragment())
                else
                    (activity as? MyAccountActivity)?.replaceFragment(ContactUsFinancialServiceFragment())
            }
            R.id.customerService -> {
                if (activity is BottomNavigationActivity)
                    mBottomNavigator?.pushFragment(ContactUsCustomerServiceFragment())
                else
                    (activity as? MyAccountActivity)?.replaceFragment(ContactUsCustomerServiceFragment())
            }
        }
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(getString(R.string.contact_us))
            showBackNavigationIcon(true)
            (activity as? BottomNavigationActivity)?.apply {
                setToolbarBackgroundDrawable(R.drawable.appbar_background)
                showToolbar()
            }
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.contact_us))

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }
}