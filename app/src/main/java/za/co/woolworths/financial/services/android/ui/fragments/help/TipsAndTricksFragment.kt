package za.co.woolworths.financial.services.android.ui.fragments.help

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TipsTricksFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ITipsAndTricksListener
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksListAdapter
import za.co.woolworths.financial.services.android.util.Utils

class TipsAndTricksFragment : Fragment(R.layout.tips_tricks_fragment), ITipsAndTricksListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var binding: TipsTricksFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = TipsTricksFragmentBinding.bind(view)
        setupToolbar()

        binding.apply {
            featureSwitch?.setOnCheckedChangeListener(this@TipsAndTricksFragment)
            featureSwitch?.isChecked = Utils.isFeatureWalkThroughTutorialsEnabled()
            val mLayoutManager = LinearLayoutManager(activity)
            mLayoutManager.orientation = LinearLayoutManager.VERTICAL
            tipsAndTricksList?.layoutManager = mLayoutManager
            tipsAndTricksList?.isNestedScrollingEnabled = false
            tipsAndTricksList?.adapter = activity?.let { TipsAndTricksListAdapter(it, this@TipsAndTricksFragment) }
        }
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            setTitle(getString(R.string.tips_tricks))
            setToolbarContentDescription(getString(R.string.toolbar_text))
            showToolbar()
        }
        if (activity is MyAccountActivity) {
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.tips_tricks))
            (activity as? MyAccountActivity)?.setToolbarContentDescription(activity?.resources?.getString(R.string.toolbar_text))
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.TIPS_AND_TRICKS_LIST)}
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden)
           setupToolbar()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        Utils.enableFeatureWalkThroughTutorials(isChecked)
    }

    override fun onListItemSelected(position: Int) {
        activity?.apply {
            val bundle = this@TipsAndTricksFragment.arguments
            val intent = Intent(this, TipsAndTricksViewPagerActivity::class.java)
            intent.putExtra("position", position)
            if (bundle != null) intent.putExtra("accounts", bundle.getString("accounts"))
            startActivityForResult(intent, BottomNavigationActivity.TIPS_AND_TRICKS_CTA_REQUEST_CODE)
           overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        }
    }
}