package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.submit_claim

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiSubmitClaimFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.bpi.SubmitClaimReason
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel.BPIViewModel
import za.co.woolworths.financial.services.android.util.Utils

class BPISubmitClaimFragment : Fragment(R.layout.bpi_submit_claim_fragment) {

    private lateinit var binding: BpiSubmitClaimFragmentBinding
    private val bpiViewModel: BPIViewModel? by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = BpiSubmitClaimFragmentBinding.bind(view)
        initRecyclerview(bpiViewModel?.bpiPresenter?.submitClaimList())
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { NavHostFragment.findNavController(this@BPISubmitClaimFragment).navigateUp() }
        })
    }

    private fun initRecyclerview(claimReasonList: List<SubmitClaimReason>?) {
        activity ?: return
        binding.bpiSubmitClaimRecyclerview?.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = BPISubmitClaimAdapter(claimReasonList){ position ->
                val selectedClaimReason  = claimReasonList?.get(position)
                view?.findNavController()?.navigate(BPISubmitClaimFragmentDirections.actionSubmitClaimToBPISubmitClaimDetailFragment(selectedClaimReason))
            }
        }
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
            changeActionBarUI(isActionBarTitleVisible = true)
            bpiViewModel?.bpiPresenter?.defaultLabel()?.claimReasonTitle?.let { setToolbarTitle(it) }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.let { Utils.setScreenName(it, FirebaseManagerAnalyticsProperties.ScreenNames.BPI_CLAIM_REASONS) }
    }
}