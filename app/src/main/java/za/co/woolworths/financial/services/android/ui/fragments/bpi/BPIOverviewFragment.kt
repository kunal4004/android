package za.co.woolworths.financial.services.android.ui.fragments.bpi

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_overview_fragment.*
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.ui.activities.bpi.BPIBalanceProtectionActivity
import za.co.woolworths.financial.services.android.ui.adapters.BPIOverviewAdapter
import za.co.woolworths.financial.services.android.util.navigateToBalanceProtectionActivity
import za.co.woolworths.financial.services.android.util.replaceFragment
import za.co.woolworths.financial.services.android.util.updateBPIList


class BPIOverviewFragment : Fragment(), BPIOverviewAdapter.OnBPIAdapterClickListener, View.OnClickListener {

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

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.bpi_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setupList()
        listener()
        setClaimButtonVisibility(updateBPIList())
    }

    private fun initialize() {
        mLayoutManager = LinearLayoutManager(this.activity!!, LinearLayout.VERTICAL, false)
        mBPIOverviewAdapter = BPIOverviewAdapter(updateBPIList(), this)
    }

    private fun listener() {
        imGoBack.setOnClickListener(this)
        tvBPIOverviewClaim.setOnClickListener(this)
    }

    private fun setupList() {
        rvBPIOverview!!.layoutManager = mLayoutManager
        rvBPIOverview!!.adapter = mBPIOverviewAdapter
    }

    private fun setClaimButtonVisibility(bpiOverviewList: ArrayList<BPIOverview>) {
        val insuranceCoveredList = arrayListOf<Boolean>()
        for (overview in bpiOverviewList) {
            insuranceCoveredList.add(overview.insuranceType!!.covered)
        }

        tvBPIOverviewClaim.visibility = if (insuranceCoveredList.contains(true)) View.VISIBLE else View.GONE
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
                if (activity is BPIBalanceProtectionActivity) {
                    val bpiBalanceProtectionActivity = activity as BPIBalanceProtectionActivity
                    bpiBalanceProtectionActivity.finishActivity()
                }
            }
        }
    }
}