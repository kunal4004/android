package za.co.woolworths.financial.services.android.shoptoggle.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Shapes
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import za.co.woolworths.financial.services.android.shoptoggle.ShopToggleData

@Composable
fun ExpandableListItem(item: ShopToggleData) {
    var isExpandable by remember {
        mutableStateOf(false)
    }

    Card(
      modifier = Modifier
          .fillMaxWidth()
          .animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
            ),
          shape = RoundedCornerShape(4.dp),
          elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),


    ){

    }



}
