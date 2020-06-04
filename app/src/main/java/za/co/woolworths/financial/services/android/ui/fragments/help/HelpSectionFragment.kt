package za.co.woolworths.financial.services.android.ui.fragments.help

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.need_help_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQFragment
import za.co.woolworths.financial.services.android.util.Utils

class HelpSectionFragment : Fragment(), View.OnClickListener {

    var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.need_help_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        if (activity is MyAccountActivity) {
            tipsAndTricks?.visibility = GONE
        }

        relFAQ?.setOnClickListener(this)
        tipsAndTricks?.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.HELP_SECTION) }
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.relFAQ -> if (activity is BottomNavigationActivity) {
                mBottomNavigator?.pushFragment(FAQFragment())
            } else {
                (activity as? MyAccountActivity)?.replaceFragment(FAQFragment())
            }
            R.id.tipsAndTricks -> {
                val tipsAndTricksFragment = TipsAndTricksFragment()
                val bundle = this.arguments
                if (bundle != null) tipsAndTricksFragment.arguments = bundle
                if (activity is BottomNavigationActivity) {
                    mBottomNavigator?.pushFragment(tipsAndTricksFragment)
                } else {
                    (activity as? MyAccountActivity)?.replaceFragment(tipsAndTricksFragment)
                }
            }
            else -> return
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
            setupToolbar()
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            setTitle(getString(R.string.need_help))
            showToolbar()
        }

        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity)?.apply {
                supportActionBar?.show()
                setToolbarTitle(activity?.resources?.getString(R.string.need_help))
            }
        }
    }
}