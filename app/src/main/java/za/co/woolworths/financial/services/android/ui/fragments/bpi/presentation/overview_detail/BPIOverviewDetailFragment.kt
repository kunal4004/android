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
import kotlinx.android.synthetic.main.bpi_detail_header.*
import kotlinx.android.synthetic.main.bpi_overview_detail_content.*
import kotlinx.android.synthetic.main.overview_detail_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.navigateUpOrFinish
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
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
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }
        arguments?.apply {
            val bpiOverview = BPIOverviewDetailFragmentArgs.fromBundle(this)
            bpiOverview.overviewArgs?.apply {
                val title =  overview?.header ?: overview?.title
                tvTitle?.text = title?.let { bindString(it) }
                benefitHeaderDrawable?.let { imBackgroundHeader?.setImageResource(it) }
                insuranceType?.let { claimVisibility(it) }
                setBenefitDetail(this)
            }
        }

        btnHowToClaim?.apply {
           text = bpiViewModel?.bpiPresenter?.defaultLabel()?.howToClaim?.let { bindString(it) }
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@BPIOverviewDetailFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPress()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            android.R.id.home -> onBackPress()
        }
        return true
    }

    private fun onBackPress() {
        NavHostFragment.findNavController(this@BPIOverviewDetailFragment).navigateUpOrFinish(activity as? BalanceProtectionInsuranceActivity)
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
    private fun setBenefitDetail(bpiOverview: BalanceProtectionInsuranceOverview?) {
        val layoutInflater = LayoutInflater.from(context)
        bpiOverview?.overview?.benefits?.forEach { desc ->
                val bpiBenefitRow = layoutInflater.inflate(R.layout.bpi_overview_benefit_row, null, false)
                val tvDescription = bpiBenefitRow.findViewById(R.id.tvDescription) as? WTextView
                tvDescription?.text = bindString(desc)
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