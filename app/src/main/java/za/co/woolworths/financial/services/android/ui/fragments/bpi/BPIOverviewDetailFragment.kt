package za.co.woolworths.financial.services.android.ui.fragments.bpi

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils
import java.text.ParseException
import java.text.SimpleDateFormat

class BPIOverviewDetailFragment : BPIFragment(), View.OnClickListener {

    companion object {
        fun newInstance(bpiOverview: BPIOverview) = BPIOverviewDetailFragment().apply {
            arguments = Bundle(1).apply {
                putString("bpiOverview", Gson().toJson(bpiOverview))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.bpi_overview_detail_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {

            if (it.containsKey("bpiOverview")) {
                val strOverview: String? = it.getString("bpiOverview")
                val bpiOverview = Gson().fromJson(strOverview, BPIOverview::class.java)
                setBenefitDetail(bpiOverview)
                val insuranceType: InsuranceType = bpiOverview.insuranceType!!
                claimVisibility(insuranceType)
                setBenefitTitle(bpiOverview)
                imBackgroundHeader.setImageResource(bpiOverview.benefitHeaderDrawable!!)
            }
        }

        btnHowToClaim.setOnClickListener(this)
        imNavigateBack.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_DETAILS)
    }

    private fun setBenefitTitle(bpiOverview: BPIOverview) {
        tvTitle.text = bpiOverview.overviewTitle
    }

    @SuppressLint("InflateParams")
    private fun setBenefitDetail(bpiOverview: BPIOverview) {
        val layoutInflater = LayoutInflater.from(context)
        for (desc in bpiOverview.benfitDescription!!) {
            val bpiBenefitRow = layoutInflater.inflate(R.layout.bpi_overview_benefit_row, null, false)
            val tvDescription = bpiBenefitRow.findViewById(R.id.tvDescription) as WTextView
            tvDescription.text = desc
            llBenefitContainer.addView(bpiBenefitRow)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun claimVisibility(insuranceType: InsuranceType) {
        tvCover.visibility = if (insuranceType.covered) VISIBLE else GONE
        llHowToClaim.visibility = if (insuranceType.covered) VISIBLE else GONE
        tvEffectiveDate.visibility = if (insuranceType.covered) VISIBLE else GONE
        // Hide EffectiveDate if insuranceType.effectiveDate is empty

        if (TextUtils.isEmpty(insuranceType.effectiveDate)) {
            tvEffectiveDate.visibility = GONE
            return
        }

        tvEffectiveDate.text = getString(R.string.bpi_effective_date) + " " + formatEffectiveDate(insuranceType.effectiveDate)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnHowToClaim -> {
                navigateToBalanceProtectionActivity()
            }

            R.id.imNavigateBack -> {
                activity?.apply {supportFragmentManager.popBackStack()  }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun formatEffectiveDate(effectiveDate: String): String {
        try {
            return SimpleDateFormat("dd/MM/yyyy").format(SimpleDateFormat("yyyy-MM-dd").parse(effectiveDate))
        } catch (ex: ParseException) {
        }
        return ""
    }
}