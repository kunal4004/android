package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel.ShopToggleViewModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily

@Composable
fun ToggleExpandableList(
    viewModel:ShopToggleViewModel,
    item: List<ToggleModel>,
) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        items(item) { item ->
            ExpandableListItem(
                item= item,
                isExpended = viewModel.expandedItemId == item.id,
                onItemClick = {
                    if (viewModel.expandedItemId == item.id){
                        viewModel.collapseItem()
                    } else {
                        viewModel.expandItem(item.id)
                    }
                }

            )
        }

    }
    justBrowsing()

}

@Composable
fun justBrowsing() {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "just browsing".uppercase(),
        textAlign = TextAlign.Center,
        textDecoration = TextDecoration.Underline,
        style = TextStyle(
            fontFamily = FuturaFontFamily,
            fontWeight = FontWeight.W500,
            fontSize = 12.sp,
            color = Color.Black
        ),

    )

}