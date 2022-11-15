package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.information

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.databinding.FragmentInformationBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.SystemBarCompat
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing.AccountProductsHomeViewModel
import javax.inject.Inject


@AndroidEntryPoint
class InformationFragment : ViewBindingFragment<FragmentInformationBinding>(FragmentInformationBinding::inflate) {

    val viewModel by viewModels<InformationViewModel>()
    val homeViewModel : AccountProductsHomeViewModel by activityViewModels()
    val args: InformationFragmentArgs by navArgs()

    @Inject
    lateinit var statusBarCompat: SystemBarCompat

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusBarCompat.setDarkStatusAndNavigationBar()
        homeViewModel.showAccountInArrearsPopup = false
        homeViewModel.isFromInformationUI = true
        setToolbar()
        setAdapter()
        onBackPressed()
    }

    fun onBackPressed() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    (activity as? StoreCardActivity)?.landingNavController()?.popBackStack()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun setAdapter() {
        binding.informationRcv.adapter = InformationAdapter(args.informationData.info)
    }

    private fun setToolbar() {
        (activity as? StoreCardActivity)?.apply {
            getToolbarHelper()?.setInformationToolbar {
                landingNavController()?.popBackStack()
            }
        }
    }
}