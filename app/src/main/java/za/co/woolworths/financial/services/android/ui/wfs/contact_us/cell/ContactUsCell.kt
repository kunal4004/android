package za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsType
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin

@Composable
fun LabelTitle(params: LabelProperties) {
    LabelTitleLarge(
        params = LabelProperties(
            label = params.label,
            stringId = params.stringId,
            fontSize = params.fontSize,
            isUpperCased = params.isUpperCased,
            style = params.style,
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
                .padding(start = Margin.start, top = 22.dp, bottom = 20.dp, end = 15.dp))
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
        MyIcon(
            id = R.drawable.ic_caret_black,
            contentDescriptionId = R.string.next_arrow,
            modifier = Modifier.alpha(if(item.type == ContactUsType.ACTION_FAX) 0f else 1f))
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
                    onClick = {
                        onOptionSelected(item)
                        onSelected(item)
                    }),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 2.dp)
            ) { LabelSmall(LabelProperties(label =  item.title))  }
            CheckedUncheckedRadioButton(isChecked = item == selectedOption,
                onClick = {
                    onOptionSelected(item)
                    onSelected(item)
                })
        }
        DividerThicknessOne()
    }
}

@Composable
fun SingleTextViewRow(params: LabelProperties, isSelected : Boolean = false,  onclick : (String) -> Unit  ){
    Column {
        val label = params.label ?: params.stringId?.let { stringResource(id = it) } ?: ""
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(label)
                .selectable(
                    selected = isSelected,
                    onClick = { onclick(label) }
                )
                .padding(top = 24.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelSmall(params)
        }
    }
}

@Composable
fun SingleTextViewTitleRow(params: LabelProperties){
    val label = params.label ?: params.stringId?.let { stringResource(id = it) } ?: stringResource(id = R.string.app_name)
    Column {
        Row(
            modifier = Modifier
                .testTag(label)
                .fillMaxWidth()
                .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelTitleCustomStyleLarge(params)
        }
    }
}
