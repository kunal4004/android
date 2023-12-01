package za.co.woolworths.financial.services.android.ui.fragments.help

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import com.awfs.coordination.R
import com.awfs.coordination.databinding.NeedHelpFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigator
import za.co.woolworths.financial.services.android.ui.fragments.faq.FAQFragment
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class HelpSectionFragment : BaseFragmentBinding<NeedHelpFragmentBinding>(
    NeedHelpFragmentBinding::inflate
), View.OnClickListener {

    var mBottomNavigator: BottomNavigator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationActivity)
            mBottomNavigator = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        if (activity is MyAccountActivity) {
            binding.tipsAndTricks.visibility = GONE
        }

        binding.relFAQ.setOnClickListener(this)
        binding.tipsAndTricks.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            Utils.setScreenName(
                it,
                FirebaseManagerAnalyticsProperties.ScreenNames.HELP_SECTION
            )
        }
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
            setToolbarContentDescription(getString(R.string.toolbar_text))
            showToolbar()
        }

        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity)?.apply {
                supportActionBar?.show()
                setToolbarTitle(activity?.resources?.getString(R.string.need_help))
                setToolbarContentDescription(getString(R.string.toolbar_text))
            }
        }
    }
}