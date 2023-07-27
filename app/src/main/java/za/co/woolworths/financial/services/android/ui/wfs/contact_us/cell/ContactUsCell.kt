package za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.getAccessibilityIdWithAppendedString
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme


@Preview
@Composable
fun TitleDescriptionAndNextArrowItemPreview() {

    val children = Children(
         title= "General enquiries",
        description = "Website and App Shopping",
        children =mutableListOf()
    )

    val childItem = Children(
        title= "Pet Insurance",
        description = "Website and App Shopping",
        type= ContactUsType.ACTION_EMAIL_INAPP,
        children =mutableListOf()
    )

    val enquiryOption = ChildrenItem(
        order = 1.0f,
        title = "Black Credit Card Query",
        description = null,
        reference = "ENQUIRY_BLACK_CREDIT_CARD_QUERY_EMAIL",
        type = null,
        children = mutableListOf()
    )

    OneAppTheme {
        Column (modifier = Modifier.background(Color.White)){
            TextContactUsFuturaSemiBoldSectionHeader(stringResource(id = R.string.contact_us_financial_services), locator ="")
            SpacerHeight6dp()
            TitleDescriptionAndNextArrowItem(children, false)
            SpacerHeight6dp()
            LeftIconTitleDescriptionAndNextArrowItem(childItem)
            SpacerHeight6dp()
            TextWithRadioButtonOption(item = enquiryOption, selectedOption = enquiryOption , onOptionSelected = {}, onSelected = {})
        }
    }
}

@Composable
fun TitleDescriptionAndNextArrowItem(children: Children, isAppendString: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Margin.start,
                top = Margin.dp16,
                bottom = Margin.dp16,
                end = Margin.dp13
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleString = children.title ?: ""
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            var titleAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "title")
            if (isAppendString) { titleAccessibilityId = titleAccessibilityId.plus("_1") }

            TextOpenSansSemiBoldH3(
                text =  children.title ?: "" ,
                color = Color.Black,
                fontSize = FontDimensions.sp13,
                locator = titleAccessibilityId
            )
            var descriptionAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "subtitle")
            if (isAppendString) { descriptionAccessibilityId = descriptionAccessibilityId.plus("_1") }

            children.description?.let { description ->
                SpacerHeight12dp(height = Dimens.two_dp)
                TextOpenSansFontFamily(
                    text =description,
                    locator = descriptionAccessibilityId,
                    color = Color.Black,
                    fontSize = FontDimensions.sp12
                )
            }
        }
        val nextArrowAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "next_arrow")
        MyIcon(id = R.drawable.ic_caret_black, contentDescriptionId = R.string.next_arrow, locator = nextArrowAccessibilityId)
    }
}

@Composable
fun LeftIconTitleDescriptionAndNextArrowItem(item: Children) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Margin.start,
                top = Margin.dp16,
                bottom = Margin.dp16,
                end = Margin.dp13
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val titleString = item.title ?: ""
        val imageIconAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "image_icon")

        MyIcon(id = item.type?.iconId(), modifier = Modifier, locator = imageIconAccessibilityId)
        SpacerWidth16dp()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            val titleAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "title")

            TextOpenSansSemiBoldH3(
                text =  item.title ?: "" ,
                color = Color.Black,
                fontSize = FontDimensions.sp13,
                locator = titleAccessibilityId)
            item.description?.let { description ->
                val descriptionAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "description")

                SpacerHeight8dp(height = Dimens.four_dp)
                TextOpenSansFontFamily(
                    text = description,
                    locator = descriptionAccessibilityId,
                    color = Color.Black,
                    fontSize = FontDimensions.sp12
                )
            }
        }
        val nextArrowAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "next_arrow")
        MyIcon(
            id = R.drawable.ic_caret_black,
            contentDescriptionId = R.string.next_arrow,
            modifier = Modifier.alpha(if(item.type == ContactUsType.ACTION_FAX) 0f else 1f,),
        locator = nextArrowAccessibilityId)
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
            val titleString =  item.title ?: ""
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = Margin.start,
                        end = Margin.dp2
                    )
            ) {
                val descriptionAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "title")

                TextOpenSansFontFamily(
                        text = titleString,
                        locator = descriptionAccessibilityId,
                        color = Color.Black,
                        fontSize = FontDimensions.sp12
                    )
            }
            val checkBoxAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "checkbox")
            CheckedUncheckedRadioButton(isChecked = item == selectedOption, locator = checkBoxAccessibilityId,
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
                .padding(top = Margin.top, bottom = Margin.bottom),
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
                .padding(start = Margin.start, top = Margin.top, bottom = Margin.bottom),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LabelTitleCustomStyleLarge(params)
        }
    }
}

@Composable
fun TextContactUsFuturaSemiBoldSectionHeader(title : String, locator: String){
    TextFuturaFamilyHeader1(
        text = title,
        textColor = Color.Black,
        fontWeight = FontWeight.SemiBold,
        fontSize = FontDimensions.sp16,
        modifier = Modifier
            .testAutomationTag(if (locator.isEmpty()) title else locator)
            .padding(
                start = Margin.start,
                end = Margin.end,
                top = Margin.dp22,
                bottom = Margin.dp20
            )
    )
}

