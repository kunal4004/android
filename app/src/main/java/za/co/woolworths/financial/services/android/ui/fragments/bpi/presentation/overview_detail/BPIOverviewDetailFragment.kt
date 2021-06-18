package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview_detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bpi_detail_header.*
import kotlinx.android.synthetic.main.bpi_overview_detail_content.*
import kotlinx.android.synthetic.main.bpi_overview_detail_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverviewFromConfig
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview.BPIOverviewFragment.Companion.SELECTED_ITEM
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class BPIOverviewDetailFragment : Fragment(), View.OnClickListener {

    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.overview_detail_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            val item = getString(SELECTED_ITEM, "")
            val bpiOverview: BalanceProtectionInsuranceOverviewFromConfig? =
                item?.let { Gson().fromJson(item, BalanceProtectionInsuranceOverviewFromConfig::class.java) }
            bpiOverview?.apply {
                tvTitle?.text = overview?.title ?: ""
                benefitHeaderDrawable?.let { imBackgroundHeader?.setImageResource(it) }
                insuranceType?.let { claimVisibility(it) }
                setBenefitDetail(bpiOverview)
            }
        }

        btnHowToClaim?.apply {
         AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@BPIOverviewDetailFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavHostFragment.findNavController(this@BPIOverviewDetailFragment).navigateUp()
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> bpiViewModel?.bpiPresenter?.navigateToPreviousFragment()
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    private fun claimVisibility(insuranceType: InsuranceType) {
        val insuranceTypeCovered = insuranceType.covered
        tvCover?.visibility = if (insuranceTypeCovered) View.VISIBLE else View.GONE
        llHowToClaim?.visibility = if (insuranceTypeCovered) View.VISIBLE else View.GONE
        tvEffectiveDate?.visibility = if (insuranceTypeCovered) View.VISIBLE else View.GONE
         bpiViewModel?.bpiPresenter?.apply {
             val insuranceTypeEffectiveDate = insuranceType.effectiveDate

             // Hide EffectiveDate if insuranceType.effectiveDate is empty
             if (TextUtils.isEmpty(insuranceTypeEffectiveDate)) {
                 tvEffectiveDate?.visibility = View.GONE
                 return
             }

             val effectiveDate =  effectiveDate(insuranceTypeEffectiveDate)
             tvEffectiveDate?.text = "${bindString(R.string.bpi_effective_date)} $effectiveDate"
         }
    }

    @SuppressLint("InflateParams")
    private fun setBenefitDetail(bpiOverview: BalanceProtectionInsuranceOverviewFromConfig?) {
        val layoutInflater = LayoutInflater.from(context)
        bpiOverview?.overview?.benefits?.forEach { desc ->
                val bpiBenefitRow = layoutInflater.inflate(R.layout.bpi_overview_benefit_row, null, false)
                val tvDescription = bpiBenefitRow.findViewById(R.id.tvDescription) as? WTextView
                tvDescription?.text = desc
                llBenefitContainer.addView(bpiBenefitRow)
            }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.btnHowToClaim -> {
                bpiViewModel?.bpiPresenter?.navigateTo(R.id.action_OverViewDetail_to_SubmitClaim, bundleOf())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Utils.setScreenName(activity, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_DETAILS)
    }

    override fun onResume() {
        super.onResume()
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            changeActionBarUI(isActionBarTitleVisible = false)
        }
    }
}