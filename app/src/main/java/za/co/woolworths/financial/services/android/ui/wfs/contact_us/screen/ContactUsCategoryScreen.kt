package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleLabelItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content

@Composable
fun ContactUsCategoryScreen(viewModel: ContactUsViewModel, onSelected: (title: String,MutableList<ChildrenItem>) -> Unit)
{
    val contentListSharedFlow: MutableList<Content>? by viewModel.contentList.collectAsState(initial = mutableListOf())
    val isLoadingSharedFlow by viewModel.isLoadingSharedFlow.collectAsState(initial = true)
    val isFailure by viewModel.isFailureSharedFlow.collectAsState(initial = Throwable())

    if (isLoadingSharedFlow){
        ContactUsLoadingShimmer (viewModel.contentListFromRawFolder ?: mutableListOf())
    } else {
        CategoryList(contentListSharedFlow ?: mutableListOf(), onSelected)
    }
}

@Composable
fun CategoryList(
    contentList: MutableList<Content>,
    onSelected: (title: String,MutableList<ChildrenItem>) -> Unit
) {
    BoxBackground {
        ListColumn(list = contentList) { item ->
            Column {
                TitleLabelItem(item.title)
                DividerThicknessOne()
            }
            val size = item.children.size.minus(1)
            item.children.forEachIndexed { index, child ->
                val title =  if (item.title.equals(stringResource(id = R.string.contact_us_financial_services), ignoreCase = true)) item.title else child.title ?: ""
               Column  {
                   Box(Modifier.clickable { onSelected(title, child.children) }) {
                       TitleDescriptionAndNextArrowItem(child)
                   }
                   if (index == size) DividerThicknessEight() else DividerThicknessOne()
               }
            }
        }
    }
}

@Composable
fun ContactUsLoadingShimmer(contentList: MutableList<Content>) {
    ShimmerEffect { brush ->
        BoxBackground {
            ListColumn(list = contentList) { item ->
                Column {
                    ShimmerTextLabel(brush = brush)
                    DividerThicknessOne()
                }
                val size = item.children.size.minus(1)
                item.children.forEachIndexed { index, _ ->
                    Column {
                        ShimmerTitleDescriptionAndNextArrowItem(brush = brush)
                        if (index == (size)) DividerThicknessEight() else DividerThicknessOne()
                    }
                }
            }
        }
    }
}
