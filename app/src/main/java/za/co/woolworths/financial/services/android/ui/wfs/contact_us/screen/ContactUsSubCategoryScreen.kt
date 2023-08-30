package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.getAccessibilityIdWithAppendedString
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TextContactUsFuturaSemiBoldSectionHeader
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LeftIconTitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.theme.FontDimensions
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
import com.awfs.coordination.R

@Composable
fun ContactUsSubCategoryScreen(viewModel: ContactUsViewModel, onSelected: (Children) -> Unit) {
    SubCategoryList(viewModel.subCategories.second, onSelected)
}

@Composable
fun SubCategoryList(listOfChildren: MutableList<ChildrenItem>, onSelected: (Children) -> Unit) {
    BoxBackground {
        ListColumn(list = listOfChildren) { item ->
            val titleString = item.title ?: ""
            val titleAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, "")
            TextContactUsFuturaSemiBoldSectionHeader(title = titleString, locator = titleAccessibilityId)
             Column(Modifier.padding(start = Margin.start, end = Margin.dp15)) {
                    item.description?.let { desc ->
                        val descriptionAccessibilityId = titleString.getAccessibilityIdWithAppendedString(titleString, stringResource(
                            id = R.string.description
                        ))
                        TextOpenSansFontFamily(
                            text =desc,
                            textAlign = TextAlign.Start,
                            color = Color.Black,
                            locator = descriptionAccessibilityId,
                            fontSize = FontDimensions.sp12
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                DividerThicknessOne()
                val childrenSize = item.children.size.minus(1)
                item.children.forEachIndexed { index, children ->
                    Column {
                        Box(Modifier.clickable { onSelected(children) }) {
                            LeftIconTitleDescriptionAndNextArrowItem(children)
                        }
                        if (index == childrenSize) DividerThicknessEight() else DividerThicknessOne()
                    }
                }
        }
    }
}
