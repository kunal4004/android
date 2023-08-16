package za.co.woolworths.financial.services.android.shoptoggle.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.shoptoggle.ShopToggleData

@Composable
fun ToggleExpandableList(item: List<ShopToggleData>) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
       items(item) { item ->
           ExpandableListItem(item)
       }
    }
}