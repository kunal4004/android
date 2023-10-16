package za.co.woolworths.financial.services.android.presentation.common.confirmationdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ConfirmationDialogEvents
import za.co.woolworths.financial.services.android.presentation.common.confirmationdialog.components.ConfirmationDialogUiState
import za.co.woolworths.financial.services.android.ui.compose.contentView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_KEY_CONFIRMATION_DIALOG
import za.co.woolworths.financial.services.android.viewmodels.ConfirmationViewModel

@AndroidEntryPoint
class ConfirmationBottomsheetDialogFragment : WBottomSheetDialogFragment() {

    private val confirmationViewModel: ConfirmationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = contentView(
        ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
    ) {
        OneAppTheme {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {

                Spacer(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .padding(bottom = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            colorResource(id = R.color.color_D8D8D8),
                            RoundedCornerShape(2.dp)
                        )
                )

                val uiState by confirmationViewModel.uiState.collectAsStateWithLifecycle()
                when (uiState) {
                    // Delete Progress View
                    is ConfirmationDialogUiState.StateDeleteProgress -> {
                        val data by confirmationViewModel.deleteProgressViewUiState
                        DeleteProgressBarView(
                            data
                        )
                    }

                    // Delete List Confirmation
                    is ConfirmationDialogUiState.StateDeleteListConfirmation -> {
                        val data by confirmationViewModel.deleteListConfirmationUiState
                        DeleteListConfirmationView(
                            modifier = Modifier
                                .padding(top = 25.dp),
                            data,
                            onCheckBoxChange = {
                                confirmationViewModel.onEvent(
                                    ConfirmationDialogEvents.OnCheckedChange(
                                        it
                                    )
                                )
                            },
                            onConfirmClick = {
                                val bundle = confirmationViewModel.getBundleData()
                                setResultAndDismiss(bundle)
                            },
                            onCancelClick = {
                                setResultAndDismiss()
                            }
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setResultAndDismiss(bundle: Bundle = bundleOf()) {
        setFragmentResult(REQUEST_KEY_CONFIRMATION_DIALOG, bundle)
        dismiss()
    }
}