package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.component.detail.ShopOptimiserAccordionDetailPopup
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
/**
 * Fragment class for displaying Shoptimiser details in a bottom sheet dialog.
 * This fragment is used to show detailed information related to the Shoptimiser in a bottom sheet dialog.
 */
class ShoptimiserDetailsFragment : WBottomSheetDialogFragment() {

    // Obtain the shared ViewModel for ShopOptimiser
    val viewModel: ShopOptimiserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {
        // Apply the OneAppTheme to the fragment content
        OneAppTheme {
            // Display the ShoptimiserAccordionDetailPopup using the shared ViewModel
            ShopOptimiserAccordionDetailPopup(viewModel) {
                // Dismiss the bottom sheet dialog if the fragment is added and visible
                if (isAdded && isVisible) dismiss()
            }
        }
    }

}
