package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.awfs.coordination.databinding.AccountProductsHomeFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.IBottomSheetBehaviour
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.INavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.NavigationGraph
import za.co.woolworths.financial.services.android.ui.fragments.account.main.component.WBottomSheetBehaviour

class UIComponent(
    bottomSheet: WBottomSheetBehaviour,
    graph: NavigationGraph
) : IBottomSheetBehaviour by bottomSheet,
    INavigationGraph by graph

@AndroidEntryPoint
class AccountProductsHomeFragment : ViewBindingFragment<AccountProductsHomeFragmentBinding>() {

    private var sheetBehavior: BottomSheetBehavior<*>? = null

    private lateinit var uiComponent: UIComponent

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): AccountProductsHomeFragmentBinding {
        return AccountProductsHomeFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiComponent = UIComponent(WBottomSheetBehaviour(requireContext()), NavigationGraph())
        setupBottomSheet()
    }

    private fun setupBottomSheet() {
        with(uiComponent) {
            with(binding) {
                sheetBehavior = init(this?.bottomSheetBehaviourView)
                sheetBehavior?.addBottomSheetCallback(callback { slideOffset ->
                    val homeIcon = (activity as? StoreCardActivity)?.findViewById<Button>(android.R.id.home)
                    animateDim(slideOffset, this?.dimView)
                    animateDim(slideOffset, homeIcon)
                    //animateDim(slideOffset, (activity as? StoreCardActivity)?.binding?.accountToolbar)
                    homeIcon?.rotation = slideOffset * 90
                })
            }
        }
    }
}