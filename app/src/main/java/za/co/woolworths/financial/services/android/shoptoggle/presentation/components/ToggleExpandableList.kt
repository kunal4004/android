package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color666666
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

@Composable
fun ToggleExpandableList(
    viewModel: ShopToggleViewModel,
    item: List<ToggleModel>,
) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(Dimens.dp24)
    ) {

        items(item) { item ->
            ExpandableListItem(
                item = item,
                isExpanded = viewModel.expandedItemId == item.id ,
                onItemClick = {

                    if (viewModel.expandedItemId == item.id) {
                        viewModel.collapseItem()
                    } else {
                        viewModel.expandItem(item.id)
                    }

                },
                viewModel

            )
        }

    }

   if (viewModel.isShopToggleScreenFirstTime.value) {
        JustBrowsing(viewModel)
      }


}

@Composable
fun JustBrowsing(viewModel: ShopToggleViewModel) {
    val activity = (LocalContext.current as? ShopToggleActivity)
   viewModel.disableFirstTimeShopToggleScreen(false)
    Spacer(modifier = Modifier.height(Dimens.thirty_one_dp))
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(true) {
                activity?.finish()
            },
        text = stringResource(id = R.string.just_browsing).uppercase(),
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = Dimens.twelve_sp,
            color = Color666666
        ),

        )

}