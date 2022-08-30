package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountProductsHomeFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.IBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local.AccountDataClass
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import javax.inject.Inject

class UIComponent(
    bottomSheet: WBottomSheetBehaviour,
) : IBottomSheetBehaviour by bottomSheet

@AndroidEntryPoint
class AccountProductsHomeFragment : Fragment(R.layout.account_products_home_fragment) {

    private var sheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var uiComponent: UIComponent

    val homeViewModel: AccountProductsHomeViewModel by activityViewModels()

    @Inject
    lateinit var accountDataClass: AccountDataClass
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = AccountProductsHomeFragmentBinding.bind(view)
        uiComponent = UIComponent(
            WBottomSheetBehaviour(
                requireContext(), AccountProductLandingDao(
                    accountDataClass
                )
            )
        )
        setupBottomSheet(binding)
    }

    private fun setupBottomSheet(binding: AccountProductsHomeFragmentBinding) {
        with(uiComponent) {

            with(binding) {
                sheetBehavior = init(bottomSheetBehaviourView)
                sheetBehavior?.addBottomSheetCallback(callback { slideOffset ->
                    val homeIcon = (activity as? StoreCardActivity)?.getBackIcon()
                    homeIcon?.rotation = slideOffset * -90
                    homeViewModel.bottomSheetBehaviorState =
                        if (slideOffset == 1.0f) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
                    animateDim(slideOffset, dimView)
                })
            }

            lifecycleScope.launch {
                homeViewModel.isBottomSheetBehaviorExpanded.collectLatest { isExpanded ->
                    if (isExpanded) {
                        homeViewModel.setIsBottomSheetBehaviorExpanded(false)
                        sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
        }
    }

}