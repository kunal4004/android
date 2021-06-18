package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.balance_protection_insurance_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverviewFromConfig
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.util.Utils

class BPIOverviewFragment : Fragment() {

    private val bpiViewModel: BPIViewModel? by activityViewModels()

    companion object {
        const val SELECTED_ITEM = "SELECTED_ITEM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.balance_protection_insurance_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }
        initRecyclerview(bpiViewModel?.bpiPresenter?.coveredUncoveredList())

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finishActivity() }
        })
    }

    private fun initRecyclerview(coveredUncoveredList: MutableList<BalanceProtectionInsuranceOverviewFromConfig>?) {
        activity ?: return
        bpiOverviewRecyclerview?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = BalanceProtectionInsuranceAdapter(coveredUncoveredList){
                overview ->
                bpiViewModel?.bpiPresenter?.navigateTo(R.id.action_Overview_to_OverViewDetail,
                    bundleOf(SELECTED_ITEM to Gson().toJson(overview)))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bpi_claim, menu)
        val claimButton = menu.findItem(R.id.menu_claim_button)
        claimButton?.isVisible = bpiViewModel?.bpiPresenter?.isCovered() ?: false
        claimButton.actionView?.setOnClickListener {
            bpiViewModel?.bpiPresenter?.navigateTo(R.id.action_Overview_to_SubmitClaim, bundleOf())
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> finishActivity()

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun finishActivity() {
        activity?.apply {
            finish()
            overridePendingTransition(0, 0)
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_OVERVIEW) }
    }

    override fun onResume() {
        super.onResume()
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            changeActionBarUI(R.color.white, isActionBarTitleVisible = true)
            setToolbarTitle(R.string.overview)
        }
    }
}