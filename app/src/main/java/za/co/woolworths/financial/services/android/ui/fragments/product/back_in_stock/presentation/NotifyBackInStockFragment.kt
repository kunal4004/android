package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.presentation.common.ProgressView
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreen
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreenEvents
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel.NotifyBackInStockViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@OptIn(ExperimentalComposeUiApi::class)
@AndroidEntryPoint
class NotifyBackInStockFragment : Fragment() {

    private val viewModel: NotifyBackInStockViewModel by viewModels()
    private var otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>> = linkedMapOf()
    private var selectedSku: OtherSkus? = null
    private var selectedGroupKey: String? = null
    private var hasColor: Boolean = false
    private var hasSize: Boolean = false

    companion object {
        const val OTHER_SKUSBYGROUP_KEY = "otherSKUsByGroupKey"
        const val SELECTED_SKU = "selectedSku"
        const val SELECTED_GROUP_KEY = "selectedGroupKey"
        const val HAS_COLOR = "hasColor"
        const val HAS_SIZE = "hasSize"
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                otherSKUsByGroupKey = getSerializable(
                    OTHER_SKUSBYGROUP_KEY,
                    OtherSkus::class.java
                ) as LinkedHashMap<String, ArrayList<OtherSkus>>
                selectedSku = getParcelable(SELECTED_SKU, OtherSkus::class.java)
            } else {
                otherSKUsByGroupKey =
                    getSerializable(OTHER_SKUSBYGROUP_KEY) as LinkedHashMap<String, ArrayList<OtherSkus>>
                selectedSku = getParcelable<OtherSkus>(SELECTED_SKU)
            }
            selectedGroupKey = getString(SELECTED_GROUP_KEY)
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

            when {
                backInStockState.isConfirmInProgress -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(ColorD8D8D8)
                        )

                        ProgressView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 290.dp),
                            title = stringResource(
                                id = R.string.please_wait,
                                ""
                            ),
                            desc = stringResource(id = R.string.processing_your_request_desc)
                        )
                    }
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
                                if (childFragmentManager.backStackEntryCount > 0) {
                                    childFragmentManager.popBackStack()
                                } else
                                    activity?.onBackPressed()
                            }
                            else -> viewModel.onEvent(event)
                        }
                    }
                }
            }
        }
    }

}