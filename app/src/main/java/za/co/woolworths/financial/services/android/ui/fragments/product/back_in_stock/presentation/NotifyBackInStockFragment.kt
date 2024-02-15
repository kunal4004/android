package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.common.ClickOnDialogButton
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreen
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreenEvents
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.showProgressDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.showSuccessDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel.NotifyBackInStockViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import javax.inject.Inject

@AndroidEntryPoint
class NotifyBackInStockFragment : Fragment() {

    private val viewModel: NotifyBackInStockViewModel by viewModels()
    private var otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>? = linkedMapOf()
    private var selectedSku: OtherSkus? = null
    private var selectedGroupKey: String? = null
    private var productId: String? = null
    private var storeId: String? = null
    private var hasColor: Boolean = false
    private var hasSize: Boolean = false

    @Inject
    lateinit var errorBottomSheetDialog: CommonErrorBottomSheetDialog

    companion object {
        const val OTHER_SKUSBYGROUP_KEY = "otherSKUsByGroupKey"
        const val SELECTED_SKU = "selectedSku"
        const val SELECTED_GROUP_KEY = "selectedGroupKey"
        const val HAS_COLOR = "hasColor"
        const val HAS_SIZE = "hasSize"
        const val SOURCE_SYSTEM = "oneapp"
        const val PRODUCT_ID = "productId"
        const val STORE_ID = "storeId"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? BottomNavigationActivity)?.apply {
            hideBottomNavigationMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.apply {

            otherSKUsByGroupKey =
                getSerializable(OTHER_SKUSBYGROUP_KEY) as? LinkedHashMap<String, ArrayList<OtherSkus>>
            selectedSku = getParcelable<OtherSkus>(SELECTED_SKU)

            selectedGroupKey = getString(SELECTED_GROUP_KEY)
            productId = getString(PRODUCT_ID)
            storeId = getString(STORE_ID)
            hasColor = getBoolean(HAS_COLOR)
            hasSize = getBoolean(HAS_SIZE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {
        OneAppTheme {
            val backInStockState = viewModel.getState()
            val notifyMeState by viewModel.notifyMeState.collectAsStateWithLifecycle()

            when {
                notifyMeState.isSuccess -> {
                    showSuccessDialog(backToStockUiState = backInStockState,
                        onEvent = {
                            navigateToPreviousScreen()
                        })

                }
                notifyMeState.isLoading -> {
                    showProgressDialog(backToStockUiState = backInStockState,
                        onEvent = {
                            navigateToPreviousScreen()
                        })

                }
                notifyMeState.isError -> {
                    showErrorDialog(notifyMeState.errorMessage)

                }
                else -> {
                    BackInStockScreen(
                        modifier = Modifier
                            .fillMaxHeight(),
                        backToStockUiState = backInStockState,
                        otherSKUsByGroupKey,
                        selectedGroupKey,
                        selectedSku,
                        hasColor,
                        hasSize
                    ) { event ->
                        when (event) {
                            BackInStockScreenEvents.CancelClick -> {
                                navigateToPreviousScreen()
                            }
                            else -> viewModel.onEvent(
                                hasColor,
                                hasSize,
                                event,
                                productId,
                                storeId
                            )
                        }
                    }
                }
            }
        }
    }

    private fun navigateToPreviousScreen() {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else
            activity?.onBackPressed()
    }

    private fun showErrorDialog(errorMessage: String) {
        errorBottomSheetDialog.showCommonErrorBottomDialog(
            object : ClickOnDialogButton {
                override fun onClick() {
                    navigateToPreviousScreen()
                }

                override fun onDismiss() {
                }
            },
            requireActivity(),
            getString(R.string.generic_error_something_wrong_newline),
            errorMessage,
            getString(R.string.got_it),
            false,
            false,
        )
    }
}