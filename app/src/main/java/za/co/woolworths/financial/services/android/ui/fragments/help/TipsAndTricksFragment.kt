package za.co.woolworths.financial.services.android.ui.fragments.help

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.tips_tricks_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ITipsAndTricksListener
import za.co.woolworths.financial.services.android.ui.activities.TipsAndTricksViewPagerActivity
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksListAdapter
import za.co.woolworths.financial.services.android.util.Utils

class TipsAndTricksFragment : Fragment(), ITipsAndTricksListener, CompoundButton.OnCheckedChangeListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.tips_tricks_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()

        featureSwitch?.setOnCheckedChangeListener(this)
       featureSwitch?.isChecked = Utils.isFeatureWalkThroughTutorialsEnabled()
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.orientation = LinearLayoutManager.VERTICAL
        tipsAndTricksList?.layoutManager = mLayoutManager
        tipsAndTricksList?.isNestedScrollingEnabled = false
        tipsAndTricksList?.adapter = activity?.let { TipsAndTricksListAdapter(it, this) }
    }

    private fun setupToolbar() {
        (activity as? BottomNavigationActivity)?.apply {
            showBackNavigationIcon(true)
            setToolbarBackgroundDrawable(R.drawable.appbar_background)
            setTitle(getString(R.string.tips_tricks))
            showToolbar()
        }
        if (activity is MyAccountActivity)
            (activity as? MyAccountActivity)?.setToolbarTitle(activity?.resources?.getString(R.string.tips_tricks))
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