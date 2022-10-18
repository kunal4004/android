package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import za.co.woolworths.financial.services.android.ui.wfs.component.BoxBackground
import za.co.woolworths.financial.services.android.ui.wfs.component.ListColumn
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TextWithRadioButtonOption
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem

@Composable
fun SelectEnquiryTypeList(radioOptionsList: MutableList<ChildrenItem>, onSelected : (ChildrenItem) -> Unit) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(ChildrenItem()) }
    BoxBackground {
        ListColumn(list = radioOptionsList) {  item ->
                TextWithRadioButtonOption(item, selectedOption, onOptionSelected,onSelected)
        }
    }
}