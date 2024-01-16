package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
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
                    "otherSKUsByGroupKey",
                    OtherSkus::class.java
                ) as LinkedHashMap<String, ArrayList<OtherSkus>>
                selectedSku = getParcelable("selectedSku", OtherSkus::class.java)
            } else {
                otherSKUsByGroupKey =
                    getSerializable("otherSKUsByGroupKey") as LinkedHashMap<String, ArrayList<OtherSkus>>
                selectedSku = getParcelable<OtherSkus>("selectedSku")
            }
            selectedGroupKey = getString("selectedGroupKey")
            hasColor = getBoolean("hasColor")
            hasSize = getBoolean("hasSize")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = contentView(
        ViewCompositionStrategy.DisposeOnDetachedFromWindow
    ) {

        OneAppTheme {
            // If `lifecycleOwner` changes, dispose and reset the effect
            DisposableEffect(viewLifecycleOwner) {
                val observer = LifecycleEventObserver { _, _ -> }

                // Add the observer to the lifecycle
                viewLifecycleOwner.lifecycle.addObserver(observer)

                // When the effect leaves the Composition, remove the observer
                onDispose {
                    viewLifecycleOwner.lifecycle.removeObserver(observer)
                }
            }


            val listState = viewModel.getState()
            when {

                listState.isConfirmInProgress -> {
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
                        backToStockUiState = NotifyBackInStockViewModel.BackToStockUiState(),
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


}