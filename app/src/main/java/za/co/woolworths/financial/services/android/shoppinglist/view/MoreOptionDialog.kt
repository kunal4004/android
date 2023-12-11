package za.co.woolworths.financial.services.android.shoppinglist.view

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.shoppinglist.component.MoreOptionsElement
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun MoreOptionDialog(selectedItemCount:Int, itemCopy:()->Unit, itemMove:()->Unit,itemRemove:()->Unit) {

    val list = ArrayList<MoreOptionsElement>()
    val context = LocalContext.current
    list.add(MoreOptionsElement(R.drawable.ic_copy, context.getString(R.string.copy_to_list)))
    val optionsList =  remember { list }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp)
    ) {

        Spacer(
            modifier = Modifier
                .width(50.dp)
                .height(5.dp)
                .background(ColorD8D8D8)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = context.getString(R.string.edit_items,selectedItemCount),
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W600,
                color = Color.Black
            ),
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        LazyColumn (modifier = Modifier.fillMaxWidth()){
            items(optionsList.size) { index ->
                optionsList.getOrNull(index)
                    ?.let { MoreOptionDialogCell(it.img, it.title, index, itemCopy, itemMove, itemRemove) }
            }
        }

        Box(
            modifier = Modifier.padding(top = 48.dp, bottom = 8.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Spacer(
                modifier = Modifier
                    .width(134.dp)
                    .height(5.dp)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun MoreOptionDialogCell(
    image: Int,
    title: String,
    index: Int,
    itemCopy: () -> Unit,
    itemMove: () -> Unit,
    itemRemove: () -> Unit
) {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 19.dp)
            .height(1.dp)
            .background(Color(R.color.color_D8D8D8))
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 19.dp)
            .clickable {
                when (index) {
                    0 -> itemCopy()
                    1 -> itemMove()
                    2 -> itemRemove()
                }
            }
    ) {

        Image(
            painter = painterResource(id = image),
            contentDescription = "" ,
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.CenterVertically)
        )
        Text(
            text = title,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = OpenSansFontFamily,
                fontWeight = FontWeight.W400,
                color = Color.Black,
            ),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 10.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMoreOptionDialog() {
    MoreOptionDialog(3, {},{},{})
}