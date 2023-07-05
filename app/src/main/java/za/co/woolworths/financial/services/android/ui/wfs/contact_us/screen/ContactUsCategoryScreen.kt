package za.co.woolworths.financial.services.android.ui.wfs.contact_us.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.awfs.coordination.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.wfs.common.ButtonEvent
import za.co.woolworths.financial.services.android.ui.wfs.common.ConnectionState
import za.co.woolworths.financial.services.android.ui.wfs.common.FailureScenario
import za.co.woolworths.financial.services.android.ui.wfs.common.connectivityState
import za.co.woolworths.financial.services.android.ui.wfs.component.*
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.cell.TextContactUsFuturaSemiBoldSectionHeader
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
                 labelDescriptionWithPhoneNumber("We are unable to process your request, Please contact our call centre on \n0861 50 20 10"))
                     //serverErrorResponse?.desc))
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

            TextContactUsFuturaSemiBoldSectionHeader(title = item.title ?: "")
            DividerThicknessOne()

            val size = item.children.size.minus(1)
            item.children.forEachIndexed { index, child ->
                // Only "Financial Services > General Enquiries" will have
                // the category's title in the next screen instead of the sub-category's title
                val title = if (
                    item.title.equals(
                        stringResource(id = R.string.contact_us_financial_services),
                        ignoreCase = true
                    )
                    && child.title.equals(
                        stringResource(id = R.string.contact_us_general_enquiries),
                        ignoreCase = true
                    )
                ) item.title else child.title ?: ""
                Column {
                    Box(modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(
                            bounded = true,
                            color = TitleSmall
                        ),
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
                    )) { TitleDescriptionAndNextArrowItem(child) }
                    if (index == size) DividerThicknessEight() else DividerThicknessOne()
                }
            }
        }
    }
}