package za.co.woolworths.financial.services.android.ui.fragments.bpi

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bpi_detail_header.*
import kotlinx.android.synthetic.main.bpi_overview_detail_content.*
import kotlinx.android.synthetic.main.bpi_overview_detail_fragment.*
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.navigateToBalanceProtectionActivity

class BPIOverviewDetailFragment : Fragment(), View.OnClickListener {

    companion object {
        fun newInstance(bpiOverview: BPIOverview): BPIOverviewDetailFragment {
            val bpiOverviewDetailFragment = BPIOverviewDetailFragment()
            val bundle = Bundle()
            bundle.putString("bpiOverview", Gson().toJson(bpiOverview))
            bpiOverviewDetailFragment.arguments = bundle
            return bpiOverviewDetailFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.bpi_overview_detail_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments.containsKey("bpiOverview")) {
            val strOverview: String = arguments.getString("bpiOverview")
            val bpiOverview = Gson().fromJson(strOverview, BPIOverview::class.java)
            setBenefitDetail(bpiOverview)
            val insuranceType: InsuranceType = bpiOverview.insuranceType!!
            claimVisibility(insuranceType)
            setBenefitTitle(bpiOverview)
            imBackgroundHeader.setImageResource(bpiOverview.benefitHeaderDrawable!!)
        }

        btnHowToClaim.setOnClickListener(this)
        imNavigateBack.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun setBenefitTitle(bpiOverview: BPIOverview) {
        tvTitle.text = bpiOverview.overviewTitle
        tvEffectiveDate.text = getString(R.string.bpi_effective_date) + " " + bpiOverview.insuranceType!!.effectiveDate
    }

    private fun setBenefitDetail(bpiOverview: BPIOverview) {
        val layoutInflater = LayoutInflater.from(context)
        for (desc in bpiOverview.benfitDescription!!) {
            val bpiBenefitRow = layoutInflater.inflate(R.layout.bpi_overview_benefit_row, null, false)
            val tvDescription = bpiBenefitRow.findViewById(R.id.tvDescription) as WTextView
            tvDescription.text = desc
            llBenefitContainer.addView(bpiBenefitRow)
        }
    }

    private fun claimVisibility(insuranceType: InsuranceType) {
        tvCover.visibility = if (insuranceType.covered) VISIBLE else GONE
        btnHowToClaim.visibility = if (insuranceType.covered) VISIBLE else GONE
        tvEffectiveDate.visibility = if (insuranceType.covered) VISIBLE else GONE
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnHowToClaim -> {
                navigateToBalanceProtectionActivity()
            }

            R.id.imNavigateBack -> {
                if (activity == null) return
                activity.supportFragmentManager.popBackStack()
            }
        }
    }
}