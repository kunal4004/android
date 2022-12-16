package za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem

@Composable
fun LabelTitle(params: LabelProperties) {
    LabelTitleLarge(
        params = LabelProperties(
            label = params.label,
            stringId = params.stringId,
            fontSize = params.fontSize,
            isUpperCased = params.isUpperCased,
            letterSpacing = params.letterSpacing,
            modifier = params.modifier
            .fillMaxWidth(),
            textColor = params.textColor,
            textAlign = params.textAlign)
    )
}

@Composable
fun LabelMediumText(params: LabelProperties){
    LabelMedium(
        LabelProperties(
            label = params.label,
            isUpperCased = params.isUpperCased,
            textDecoration = params.textDecoration,
            stringId = params.stringId,
            textAlign = TextAlign.Center,
            fontSize = params.fontSize,
            textColor = params.textColor,
            letterSpacing = params.letterSpacing,
            modifier = params.modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 22.dp, bottom = 20.dp, end = 15.dp))
    )}

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
            LabelMedium(LabelProperties(label = children.title))
            LabelSmall(LabelProperties(label = children.description))
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
           LabelMedium(LabelProperties(label = item.title))
           LabelSmall(LabelProperties(label = item.description))
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
            ) { LabelSmall(LabelProperties(label =  item.title))  }
            CheckedUncheckedRadioButton(isChecked = item == selectedOption)
        }
        DividerThicknessOne()
    }
}