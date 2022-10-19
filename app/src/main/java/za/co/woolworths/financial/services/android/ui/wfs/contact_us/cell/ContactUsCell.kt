package za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem

@Composable
fun TitleLabelItem(title: String?) {
    TitleLargeText(
        text = title ?: "", modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 22.dp, bottom = 20.dp, end = 15.dp)
    )
}

@Composable
fun TitleDescriptionAndNextArrowItem(children: Children) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, bottom = 16.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            children.title?.let { title -> TitleMediumText(title) }
            children.description?.let { description -> TitleSmallText(description) }
        }
        MyIcon(id = R.drawable.ic_caret_black, contentDescriptionId = R.string.next_arrow)
    }
}

@Composable
fun LeftIconTitleDescriptionAndNextArrowItem(item: Children) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 16.dp, bottom = 16.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyIcon(id = item.type?.iconId(), modifier = Modifier.padding(end = 16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item.title?.let { TitleMediumText(item.title) }
            item.description?.let { description -> TitleSmallText(description) }
        }
        MyIcon(id = R.drawable.ic_caret_black, contentDescriptionId = R.string.next_arrow)
    }
}

@Composable
fun TextWithRadioButtonOption(
    item: ChildrenItem,
    selectedOption: ChildrenItem,
    onOptionSelected: (ChildrenItem) -> Unit,
    onSelected: (ChildrenItem) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = (item == selectedOption),
                    onClick = { onOptionSelected(item)
                        onSelected(item)}),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).padding(start = 24.dp, end = 2.dp)
            ) { item.title?.let { TitleSmallText(it) } }
            CheckedUncheckedRadioButton(isChecked = item == selectedOption)
        }
        DividerThicknessOne()
    }
}