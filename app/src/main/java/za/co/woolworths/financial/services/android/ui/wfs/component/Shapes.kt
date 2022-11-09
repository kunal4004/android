package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun CircularProgress() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.147f)
            .padding(10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(), onDraw = {
            val canvasHeight = size.height
            val canvasWidth = size.width
            drawCircle(
                color = Color.Black,
                center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
                radius = size.minDimension / 2,
                style = Stroke(15F)
            )
        })
    }
}