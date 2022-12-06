package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.submit_claim_detail

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiSubmitClaimDetailFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.bpi.SubmitClaimReason
import za.co.woolworths.financial.services.android.ui.adapters.RequiredFormAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class BPISubmitClaimDetailFragment : Fragment(R.layout.bpi_submit_claim_detail_fragment), View.OnClickListener {

    private lateinit var binding: BpiSubmitClaimDetailFragmentBinding
    private val bpiViewModel: BPIViewModel? by activityViewModels()

    private var claimReasonArgs : SubmitClaimReason? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = BpiSubmitClaimDetailFragmentBinding.bind(view)
        binding.initView()
    }

    private fun BpiSubmitClaimDetailFragmentBinding.initView() {
        arguments?.apply {
            claimReasonArgs = BPISubmitClaimDetailFragmentArgs.fromBundle(this).claimReasonList
            val requiredFormAdapter = RequiredFormAdapter(claimReasonArgs?.requiredForm, true)
            val requiredFormSubmitAdapter = RequiredFormAdapter(claimReasonArgs?.requiredSubmit, false)

            bpiViewModel?.bpiPresenter?.defaultLabel()?.apply {
                applyLoanInfoTextView?.text = bindString(submitDescription)
                requiredFormTextView?.text = bindString(requiredForm)
                btnGetDocument?.text = bindString(requiredDocuments)
                youWillAlsoNeedToSubmitTextView?.text = bindString(submitForm)
            }

            rlRequiredForm?.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = requiredFormAdapter
            }

            rlAdditionalSubmission?.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = requiredFormSubmitAdapter
            }
        }

        btnGetDocument?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            btnGetDocument?.setOnClickListener(this@BPISubmitClaimDetailFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                NavHostFragment.findNavController(this@BPISubmitClaimDetailFragment).navigateUp()
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

    override fun onResume() {
        super.onResume()
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            changeActionBarUI(colorId = R.color.white, isActionBarTitleVisible = true)
            claimReasonArgs?.title?.let { setToolbarTitle(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let {
            Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_DOCUMENTS_PROCESSING)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnGetDocument -> KotlinUtils.openLinkInInternalWebView(activity, BPIViewModel.externalURL, false, null)
        }
    }
}