package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigMaritalStatus
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.common.contentView
import za.co.woolworths.financial.services.android.ui.wfs.component.DialogDropdownIndicator
import za.co.woolworths.financial.services.android.ui.wfs.component.Divider1dp
import za.co.woolworths.financial.services.android.ui.wfs.component.LabelProperties
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.SingleTextViewRow
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.SingleTextViewTitleRow
import za.co.woolworths.financial.services.android.ui.wfs.theme.*
import javax.inject.Inject

class MaritalStatusSelectionFragment : WBottomSheetDialogFragment() {

    val viewModel : MaritalStatusViewModel by activityViewModels()

    companion object {
         val MaritalStatusResultCode : String = CLIMaritalStatusFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    = contentView(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)) {
        OneAppTheme {
                MaritalStatusSelector(viewModel) {  result -> setFragmentResult(MaritalStatusResultCode, bundleOf(MaritalStatusResultCode to result))
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(MaritalStatusResultCode, bundleOf(MaritalStatusResultCode to MaritalStatusResultCode))
    }
}

@Composable
fun MaritalStatusSelector(viewModel :MaritalStatusViewModel, action : (MaritalStatusSelection) -> Unit) {
    OneAppTheme {

        val selectedItem by remember{ mutableStateOf("") }

        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.background(White)
        ) {

            item { DialogDropdownIndicator() }

            item {
                SingleTextViewTitleRow(
                    params = LabelProperties(
                        stringId = R.string.select_your_marital_status,
                        textAlign = TextAlign.Center,
                        isUpperCased = true,
                        letterSpacing = 1.sp,
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = HeaderGrey,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        ),
                         modifier = Modifier.padding(bottom = 5.dp)
                    )
                )
            }

            items(viewModel.maritalStatusArrayFromMobileConfig) { status ->
                Divider1dp()
                SingleTextViewRow(
                    params = LabelProperties(
                        letterSpacing = 1.sp,
                        label = status.statusDesc,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = if (selectedItem == status.statusDesc) FontWeight.Bold else FontWeight.Light,
                            fontSize = 12.sp,
                            color = if (selectedItem == status.statusDesc) Obsidian else HeaderGrey,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        ),
                        isUpperCased = true
                    ), isSelected = selectedItem == status.statusDesc
                ) {
                    action(MaritalStatusSelection.OnSelected(status))
                }
            }

            item {
                Divider1dp()
                SingleTextViewRow(
                    params = LabelProperties(
                        letterSpacing = 1.sp,
                        stringId = R.string.cancel,
                        textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline,
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = HeaderGrey,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        ),
                        isUpperCased = true
                    )
                ){
                    action(MaritalStatusSelection.OnCancel)
                }
            }
        }
    }
}

sealed class MaritalStatusSelection : Parcelable {
    @Parcelize data class OnSelected(val configMaritalStatus : ConfigMaritalStatus) : MaritalStatusSelection()
    @Parcelize object OnCancel : MaritalStatusSelection()
}

@HiltViewModel
class MaritalStatusViewModel @Inject constructor() : ViewModel() {

    val maritalStatusArrayFromMobileConfig = AppConfigSingleton.creditLimitIncrease?.maritalStatus ?: mutableListOf()

    fun isCliStatusId6(selectedStatusCode : Int?) = selectedStatusCode == 6
}