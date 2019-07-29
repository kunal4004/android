package za.co.woolworths.financial.services.android.ui.fragments.bpi

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.VisibleForTesting
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.activities.bpi.*
import za.co.woolworths.financial.services.android.ui.adapters.BPIOverviewAdapter
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.Utils


class BPIOverviewFragment : BPIFragment(), BPIOverviewAdapter.OnBPIAdapterClickListener, View.OnClickListener {

    companion object {
        fun newInstance(accountInfo: String?): BPIOverviewFragment {
            val bpiOverviewFragment = BPIOverviewFragment()
            val bundle = Bundle()
            bundle.putString("accountInfo", accountInfo)
            bpiOverviewFragment.arguments = bundle
            return bpiOverviewFragment
        }
    }

    private var mBPIOverviewAdapter: BPIOverviewAdapter? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.bpi_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setupList()
        listener()
        setClaimButtonVisibility(updateBPIList())
    }

    override fun onResume() {
        super.onResume()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_OVERVIEW) }
    }

    private fun initialize() {
        activity?.let { mLayoutManager = LinearLayoutManager(it, LinearLayout.VERTICAL, false) }
        mBPIOverviewAdapter = BPIOverviewAdapter(updateBPIList(), this)
    }

    private fun listener() {
        imGoBack.setOnClickListener(this)
        tvBPIOverviewClaim.setOnClickListener(this)
    }

    private fun setupList() {
        rvBPIOverview?.apply {
            layoutManager = mLayoutManager
            adapter = mBPIOverviewAdapter
        }
    }

    private fun setClaimButtonVisibility(bpiOverviewList: MutableList<BPIOverview>?) {
        val insuranceCoveredList: MutableList<Boolean> = mutableListOf()
        bpiOverviewList?.forEach {
            it.insuranceType?.covered?.let { covered -> insuranceCoveredList.add(covered) }
        }
        tvBPIOverviewClaim?.visibility = if (insuranceCoveredList.contains(true)) View.VISIBLE else View.GONE
    }

    override fun onItemViewClicked(bpiOverview: BPIOverview) {
        replaceFragment(
                fragment = BPIOverviewDetailFragment.newInstance(bpiOverview),
                tag = BPIOverviewDetailFragment::class.java.simpleName,
                containerViewId = R.id.flBPIContainer,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvBPIOverviewClaim -> {
                navigateToBalanceProtectionActivity()
            }

            R.id.imGoBack -> {
                activity?.apply {
                    if (this is BPIBalanceProtectionActivity) {
                        (this as? BPIBalanceProtectionActivity)?.finishActivity()
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public fun testUpdateBPIList(): MutableList<BPIOverview>? {
        return updateBPIList()
    }

    @VisibleForTesting
    public fun testCreateBPIList(): MutableList<BPIOverview>? {
        return createBPIList()
    }

    @VisibleForTesting
    public fun testGetInsuranceType(): MutableList<InsuranceType>? {
        return getInsuranceType()
    }

    @VisibleForTesting
    public fun testClaimButtonVisibility() {
        setClaimButtonVisibility(updateBPIList())
    }
}