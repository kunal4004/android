package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.wfs.common.ButtonEvent
import za.co.woolworths.financial.services.android.ui.wfs.common.ConnectionState
import za.co.woolworths.financial.services.android.ui.wfs.common.FailureScenario
import za.co.woolworths.financial.services.android.ui.wfs.common.connectivityState
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.LabelTitle
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TitleDescriptionAndNextArrowItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ChildrenItem
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsRemoteModel
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.Content
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.viewmodel.ContactUsViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.TitleSmall

sealed class ContactUsEvent {
    object Dismiss : ContactUsEvent()
    data class CategoryItemClicked(val details: Pair<String?, MutableList<ChildrenItem>>) :
        ContactUsEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ContactUsCategoryScreen(
    viewModel: ContactUsViewModel,
    onEventSelected: (ContactUsEvent) -> Unit
) {
    val connectivity by connectivityState() //This will cause re-composition on every network state change
    val isConnected = connectivity === ConnectionState.Available

    with(viewModel) {
        val content = contactUsSuccessModel?.content
        when (isLoadingSharedFlow) {
            true -> LoadingShimmerList(viewModel.getShimmerModel())
            false -> when {
                errorThrowable is Throwable -> FailureScenario(labelDescriptionWithPhoneNumber(description = stringResource(id = R.string.oops_error_message)))
                    { event -> when (event) {
                        ButtonEvent.Retry -> { queryServiceContactUs() }
                        ButtonEvent.Dismiss -> { onEventSelected(ContactUsEvent.Dismiss) }
                    }
                }

                serverErrorResponse is ServerErrorResponse -> FailureScenario(
                 labelDescriptionWithPhoneNumber(serverErrorResponse?.desc))
                { event -> when (event) {
                        ButtonEvent.Retry -> { queryServiceContactUs() }
                        ButtonEvent.Dismiss -> { onEventSelected(ContactUsEvent.Dismiss) }
                    }
                }

                contactUsSuccessModel is ContactUsRemoteModel -> CategoryList(content ?: mutableListOf(), onEventSelected)
            }
        }

        if (isConnected) {
            LaunchedEffect(true) {
                if (content.isNullOrEmpty())
                    queryServiceContactUs()
            }
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
                LabelTitle(
                    LabelProperties(
                        label = item.title,
                        modifier = Modifier.padding(
                            start = 24.dp,
                            end = 24.dp,
                            top = 22.dp,
                            bottom = 20.dp
                        )
                    )
                )
                DividerThicknessOne()
            }
            val size = item.children.size.minus(1)
            item.children.forEachIndexed { index, child ->
                val title = if (item.title.equals(
                        stringResource(id = R.string.contact_us_financial_services),
                        ignoreCase = true
                    )
                ) item.title else child.title ?: ""
                Column {
                    Box(modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(
                            bounded = true,
                            color = TitleSmall
                        ), // You can also change the color and radius of the ripple
                        onClick = {
                            onSelected(
                                ContactUsEvent.CategoryItemClicked(
                                    Pair(
                                        title,
                                        child.children
                                    )
                                )
                            )
                        }
                    )) {
                        TitleDescriptionAndNextArrowItem(child)
                    }
                    if (index == size) DividerThicknessEight() else DividerThicknessOne()
                }
            }
        }
    }
}