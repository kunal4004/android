package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.contact_us_customer_service.*
import kotlinx.android.synthetic.main.my_account_activity.*
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class ContactUsCustomerServiceFragment : Fragment(), View.OnClickListener {

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
        generalEnq?.setOnClickListener(this)
        woolworthsOnline?.setOnClickListener(this)
        wRewards?.setOnClickListener(this)
        mySchoolEnq?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.generalEnq -> openFragment(ContactUsGeneralEnquiriesFragment())
            R.id.woolworthsOnline -> openFragment(ContactUsOnlineFragment())
            R.id.wRewards -> openFragment(ContactUsWRewardsFragment())
            R.id.mySchoolEnq -> openFragment(ContactUsMySchoolFragment())
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
        mBottomNavigator?.apply {
            setTitle(activity?.resources?.getString(R.string.contact_us_customer_service))
            displayToolbar()
            showBackNavigationIcon(true)
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.contact_us_customer_service))
    }
}