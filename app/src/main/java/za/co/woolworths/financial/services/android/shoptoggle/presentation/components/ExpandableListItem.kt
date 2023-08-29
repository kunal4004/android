package za.co.woolworths.financial.services.android.shoptoggle.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.ShopToggleData
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.ui.wfs.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableListItem(
    item: ToggleModel,
    isExpended: Boolean,
    onItemClick: () -> Unit
) {
//    var isExpandable by remember {
//        mutableStateOf(false)
//    }
//    var isCollapse by remember {
//        mutableStateOf(true)
//    }

    val rotationState by animateFloatAsState(
        targetValue = if (isExpended) 180f else 0f, label = ""
    )

    val cardShape = RoundedCornerShape(8.dp)
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        modifier = Modifier
            .fillMaxSize()
            .border(
                BorderStroke(1.dp, color = if (isExpended) Color.Black else ColorD8D8D8),
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(
                shape = cardShape,
                spotColor = ColorD8D8D8,
                elevation = 8.dp
            )
//            .animateContentSize(
//                animationSpec = tween(
//                    durationMillis = 300,
//                    easing = LinearOutSlowInEasing
//                )
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
            ),
        shape = cardShape,
        onClick = {
            onItemClick()
           // isExpandable = !isExpandable
        }

    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically

            ) {

                Image(
                    painter = painterResource(id = item.icon),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier
                    .weight(7f)) {
                    if (isExpended) {
                        item.title?.let {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Start,
                                text = it.uppercase(),
                                style = TextStyle(
                                    fontFamily = FuturaFontFamily,
                                    fontWeight = FontWeight.W500,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                ),
                                letterSpacing = 1.5.sp
                            )
                        }
                    } else {
                        Box(modifier = Modifier
                            .width(120.dp)
                            .height(18.dp)
                            .background(ColorFEE600, shape = RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                text = "use dash delivery".uppercase(),
                                style = TextStyle(
                                    fontFamily = FuturaFontFamily,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )
                            )

                        }

                    }

                    item.subTitle?.let {
                        Text(
                            modifier = Modifier,
                            textAlign = TextAlign.Start,
                            text = it.uppercase(),
                            style = TextStyle(
                                fontFamily = FuturaFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = Color.Black
                            ),
                            letterSpacing = 1.5.sp
                        )
                    }

                }
                Image(
                    modifier = Modifier
                        .weight(1f),
                  //  .rotate(rotationState),
                    alignment = Alignment.CenterEnd,
                    painter = if (isExpended) painterResource(id = R.drawable.ic_up_arrow)
                    else painterResource(id = R.drawable.ic_dwon_arrow),
                    contentDescription = "Down Arrow")

            }

            if (isExpended) {
               // isCollapse = !isCollapse
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = ColorD8D8D8, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                item.deliveryType?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it,
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    )
                }
                item.deliveryTime?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it,
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    )
                }

                item.deliveryProduct?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it,
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                item.deliveryCost?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it,
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    )
                }

                item.learnMore?.let {
                    Text(
                        modifier = Modifier,
                        textAlign = TextAlign.Start,
                        text = it,
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                BlackButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    text = item.deliveryButtonText.uppercase(),
                    enabled = true,
                ) {
                    //

                }

            }

        }

    }

}

