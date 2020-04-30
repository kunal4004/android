package za.co.woolworths.financial.services.android.ui.fragments.faq.web

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_faqdetails_web.*
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator

class WebFragment : Fragment() {
    private var mUrl: String? = null
    private var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mUrl = getString("web_url", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_faqdetails_web, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        bindDateWithUI()
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            showBackNavigationIcon(true)
            setTitle(getString(R.string.drawer_faq))
            showToolbar()
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.woolworths_online))
    }

    private fun bindDateWithUI() {
        faqWeb?.settings?.javaScriptEnabled = true
        faqWeb?.loadUrl(mUrl)
    }
}