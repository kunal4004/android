package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.awfs.coordination.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.wfs.common.ConnectionState
import za.co.woolworths.financial.services.android.ui.wfs.common.connectivityState
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleLabelItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.Content
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.RemoteMobileConfigModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.TitleSmall

sealed class ContactUsEvent {
    data class CategoryItemClicked(val details: Pair<String?, MutableList<ChildrenItem>>) : ContactUsEvent()
    data class Response(val serverErrorResponse: ServerErrorResponse) : ContactUsEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ContactUsCategoryScreen(viewModel: ContactUsViewModel, onEventSelected: (ContactUsEvent) -> Unit)
{
    // This will cause re-composition on every network state change
    val connectivity by connectivityState()
    val isConnected = connectivity === ConnectionState.Available

    val configList : RemoteMobileConfigModel? = viewModel.remoteMobileConfig
    val remoteFailureResponse = viewModel.remoteFailureResponse

    if (viewModel.isLoadingSharedFlow){
        ContactUsLoadingShimmer (viewModel.getShimmerModel())
    } else {
        if (remoteFailureResponse is ServerErrorResponse){
            onEventSelected(ContactUsEvent.Response(remoteFailureResponse))
            return
        }
        if (configList is RemoteMobileConfigModel) {
            CategoryList(configList.content ?: mutableListOf(), onEventSelected)
        }
   }

    if (isConnected) {
        LaunchedEffect(true) {
            if (configList?.content.isNullOrEmpty())
                viewModel.queryServiceContactUs()
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun CategoryList(
    contentList: MutableList<Content>,
    onSelected: (ContactUsEvent) -> Unit
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
                   Box(modifier = Modifier.clickable(
                       interactionSource = remember { MutableInteractionSource() },
                       indication = rememberRipple(bounded = true, color = TitleSmall), // You can also change the color and radius of the ripple
                       onClick = {onSelected(ContactUsEvent.CategoryItemClicked(Pair(title, child.children)))}
                   )) {
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
