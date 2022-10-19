package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LeftIconTitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleLabelItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Children
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem

@Composable
fun ContactUsSubCategoryScreen(viewModel: ContactUsViewModel, onSelected: (Children) -> Unit) {
    SubCategoryList(viewModel.subCategories.second, onSelected)
}

@Composable
fun SubCategoryList(listOfChildren: MutableList<ChildrenItem>, onSelected: (Children) -> Unit) {
    BoxBackground {
        ListColumn(list = listOfChildren) { item ->
                TitleLabelItem(title = item.title)
                Column(Modifier.padding(start = 24.dp, end = 15.dp)) {
                    item.description?.let { desc ->
                        TitleSmallText(desc)
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
