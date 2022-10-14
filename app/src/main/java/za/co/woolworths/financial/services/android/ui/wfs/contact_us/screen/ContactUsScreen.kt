package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LeftIconTitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleLabelItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Children
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content

@Composable
fun ContactUsCategoryScreen(viewModel: ContactUsViewModel, onSelected: (MutableList<ChildrenItem>) -> Unit)
{ CategoryList(viewModel.contactUsList, onSelected) }

@Composable
fun CategoryList(contentList: MutableList<Content>,
                 onSelected: (MutableList<ChildrenItem>) -> Unit) {
    val listState = rememberLazyListState()
    Box(modifier = screenModifier) {
        LazyColumn(
            state = listState,
            modifier = lazyColumnModifier
        ) {
            items(items = contentList) { item ->
                Column {
                    TitleLabelItem(item.title)
                    DividerThicknessOne()
                }
                val childrenSize = item.children.size.minus(1)
                item.children.forEachIndexed { index, child ->
                    Column(Modifier.clickable { onSelected(child.children) }) {
                        TitleDescriptionAndNextArrowItem(child)
                        if (index == childrenSize) DividerThicknessEight() else DividerThicknessOne()
                    }
                }
            }
        }
    }
}

@Composable
fun ContactUsSubCategoryScreen(viewModel: ContactUsViewModel,  onSelected: (Children) -> Unit) {
    SubCategoryList(viewModel.subCategories, onSelected)
}

@Composable
fun SubCategoryList(contentList: MutableList<ChildrenItem>, onSelected: (Children) -> Unit) {
    val listState = rememberLazyListState()
    Box(modifier = screenModifier) {
        LazyColumn(state = listState, modifier = lazyColumnModifier) {
            items(contentList) { item ->
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
                    Column (Modifier.clickable { onSelected(children)  }) {
                        LeftIconTitleDescriptionAndNextArrowItem(children)
                        if (index == childrenSize) DividerThicknessEight() else DividerThicknessOne()
                    }
                }
            }
        }
    }
}