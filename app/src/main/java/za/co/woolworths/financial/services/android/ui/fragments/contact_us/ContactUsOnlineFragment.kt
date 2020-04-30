package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.contact_us_online.*
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.util.Utils

class ContactUsOnlineFragment : Fragment(), View.OnClickListener {
    var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.contact_us_online, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        localCaller?.setOnClickListener(this)
        internationalCaller?.setOnClickListener(this)
        onlineShop?.setOnClickListener(this)
        deliveryQueries?.setOnClickListener(this)
        technicalProblem?.setOnClickListener(this)
        orderQueries?.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        activity?.resources?.apply {
            when (v.id) {
                R.id.localCaller -> Utils.makeCall(getString(R.string.online_local_caller_number))
                R.id.internationalCaller -> Utils.makeCall(getString(R.string.online_inter_national_caller_number))
                R.id.onlineShop -> sendEmail(getString(R.string.email_online_shop), getString(R.string.online_shop_reg_login))
                R.id.deliveryQueries -> sendEmail(getString(R.string.email_online_shop), getString(R.string.txt_delivery_queries))
                R.id.technicalProblem -> sendEmail(getString(R.string.email_online_shop), getString(R.string.txt_general_technical_problem))
                R.id.orderQueries -> sendEmail(getString(R.string.email_online_shop), getString(R.string.txt_order_queries))
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
            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.INFO, getString(R.string.contact_us_no_email_error).replace("email_address", emailId).replace("subject_line", subject!!))
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setupToolbar()
        }
    }

    private fun setupToolbar() {
        mBottomNavigator?.apply {
            setTitle(activity?.resources?.getString(R.string.woolworths_online))
            displayToolbar()
            showBackNavigationIcon(true)
        }

        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.woolworths_online))
    }
}