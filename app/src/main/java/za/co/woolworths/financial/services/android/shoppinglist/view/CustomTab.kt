package za.co.woolworths.financial.services.android.shoppinglist.view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@SuppressLint("SuspiciousIndentation")
@Composable
fun CustomTabRow(
    list: List<String>,
    selectedIndex: Int,
    modifier: Modifier,
    onListOptionClick: (Int) -> Unit
) {

    Box(
        modifier = modifier
    ) {
        var updatedSelectedIndex by remember {
            mutableStateOf(selectedIndex)
        }

        updatedSelectedIndex = selectedIndex

        TabRow(
            selectedTabIndex = selectedIndex,
            modifier = Modifier
                .height(40.dp)
                .clip(RoundedCornerShape(50)),
            containerColor = Color(0xFFEEEEEE),
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = Color.Transparent
                )
            }
        ) {
            list.forEachIndexed { tabindex, text ->
                val selected = selectedIndex == tabindex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Tab(
                        modifier =
                        if (selected) Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Color.White
                            )
                        else Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Color(0xFFEEEEEE)
                            ),
                        selected = selected,
                        onClick = {
                            updatedSelectedIndex = tabindex
                        },
                        text = {
                            TabContent(text = text)
                        }
                    )
                }
                onListOptionClick(updatedSelectedIndex)
            }
        }
    }
}

@Composable
fun TabContent(text: String) {
    Text(
        text = text,
        color = Color.Black,
        textAlign = TextAlign.Center,
        fontFamily = OpenSansFontFamily,
        lineHeight = 21.sp,
        fontWeight = FontWeight.W600,
        fontSize = 14.sp
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewListOptionsTab() {
    val list = listOf(stringResource(id = R.string.my_list_option), stringResource(id = R.string.share_list_option))
    val selectedIndex by remember {
        mutableStateOf(0)
    }
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)
    ) {
        CustomTabRow(list, selectedIndex, modifier = Modifier,) {

        }
    }
}
