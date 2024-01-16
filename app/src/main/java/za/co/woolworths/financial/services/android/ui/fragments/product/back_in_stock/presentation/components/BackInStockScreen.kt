package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel.NotifyBackInStockViewModel.BackToStockUiState
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Composable
fun BackInStockScreen(
    modifier: Modifier = Modifier,
    backToStockUiState: BackToStockUiState = BackToStockUiState(),
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    selectedGroupKey: String?,
    selectedSku: OtherSkus?,
    hasColor: Boolean,
    hasSize: Boolean,
    onEvent: (event: BackInStockScreenEvents) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color.White)
            .then(modifier)
            .verticalScroll(rememberScrollState())

    ) {
        Box(
            Modifier
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            HeaderView(
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp),
                headerViewState = HeaderViewState.HeaderStateType3(
                    title = ""
                )
            ) {
                onEvent(BackInStockScreenEvents.CancelClick)
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .fillMaxHeight(), contentAlignment = Alignment.TopCenter
        ) {

            AddBISView(
                modifier = modifier,
                backToStockUiState,
                otherSKUsByGroupKey,
                selectedGroupKey,
                selectedSku,
                hasColor,
                hasSize
            )
        }

        Box(
            Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {

            BlackButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .padding(bottom = 24.dp)
                    .height(50.dp),
                text = stringResource(id = R.string.confirm).uppercase(),
                enabled = backToStockUiState.selectedSize.isNotEmpty()
            ) {
                onEvent(BackInStockScreenEvents.ConfirmClick)
            }
        }
    }

    /* if (listUiState.isLoading) {
         CircularProgressIndicator(
                 color = Color.Black
         )
     }*/
}

@Composable
private fun AddBISView(
    modifier: Modifier = Modifier,
    backToStockUiState: BackToStockUiState = BackToStockUiState(),
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    selectedGroupKey: String?,
    selectedSku: OtherSkus?,
    hasColor: Boolean,
    hasSize: Boolean
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Set up Back in Stock notifications",

            style = TextStyle(
                fontSize = 18.sp,
                textAlign = TextAlign.Start,
                lineHeight = 27.sp,
                fontFamily = FontFamily(Font(R.font.futura_semi_bold)),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF000000),
            ),
            modifier = Modifier
                .padding(start = 24.dp, top = 19.dp, end = 24.dp, bottom = 0.dp)
                .fillMaxWidth()
        )

        Text(
            text = "Enter your email address and if this product is back in stock you'll be the first to know!",

            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 19.5.sp,
                fontFamily = FontFamily(Font(R.font.opensans_medium)),
                fontWeight = FontWeight.Normal,
                color = Color(0xFF808080),
            ),
            modifier = Modifier
                .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 0.dp)
                .fillMaxWidth()
        )

        if (hasColor) {
            Text(
                text = "Select a Colour: *",

                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 19.5.sp,
                    fontFamily = FontFamily(Font(R.font.opensans_medium)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF000000),
                ),
                modifier = Modifier
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
                    .fillMaxWidth()
            )

            otherSKUsByGroupKey[selectedGroupKey]?.let {
                if (selectedGroupKey != null) {
                    SpinnerColourView(
                        otherSKUsByGroupKey,
                        selectedGroupKey,
                        modifier = Modifier
                            .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                            .fillMaxWidth(),
                        preselectedColour = selectedGroupKey
                    ) { selectedColour -> /* do something with selected */
                    }
                }
            }
        }
        if (hasSize) {
            Text(
                text = "Select a Size: *",

                style = TextStyle(
                    fontSize = 13.sp,
                    lineHeight = 19.5.sp,
                    fontFamily = FontFamily(Font(R.font.opensans_medium)),
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF000000),
                ),
                modifier = Modifier
                    .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
                    .fillMaxWidth()
            )

            otherSKUsByGroupKey[selectedGroupKey]?.let { otherSKUList ->
                val zeroQuantityList = ArrayList<OtherSkus>()
                otherSKUList.forEach { otherSKU ->
                    if (otherSKU.quantity == 0) {
                        zeroQuantityList.add(otherSKU)
                    }
                }
                otherSKUsByGroupKey.remove(selectedGroupKey)?.let {
                    otherSKUsByGroupKey.put(selectedGroupKey.toString(), zeroQuantityList)
                }
                SpinnerSizeView(
                    otherSKUsByGroupKey,
                    selectedGroupKey,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    //  preselectedSize = if(selectedSku!= null && selectedSku.quantity == 0) selectedSku else  zeroQuantityList[0]
                    preselectedSize = selectedSku

                ) { selectedSize -> /* do something with selected */
                    backToStockUiState.selectedSize = selectedSize
                }
            }
        }
        Text(
            text = "Email Address: *",

            style = TextStyle(
                fontSize = 13.sp,
                lineHeight = 19.5.sp,
                fontFamily = FontFamily(Font(R.font.opensans_medium)),
                fontWeight = FontWeight.Normal,
                color = Color(0xFF000000),
            ),
            modifier = Modifier
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
                .fillMaxWidth()
        )

        var text by remember { mutableStateOf(AppInstanceObject.getCurrentUsersID()) }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            readOnly = true,
            enabled = false,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                disabledContainerColor = Color.White
            ),
            modifier = Modifier
                .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                .border(width = 0.dp, color = Color(R.color.color_9D9D9D))
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontFamily = FontFamily(Font(R.font.opensans_medium)),
                fontWeight = FontWeight(400),
                color = Color(0xFF000000)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinnerColourView(
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    selectedGroupKey: String?,
    modifier: Modifier = Modifier,
    preselectedColour: String,
    onSelectionChanged: (selectedColour: String) -> Unit
) {

    var selectedColour by remember { mutableStateOf(preselectedColour) }
    var expanded by remember { mutableStateOf(false) } // initial value
    val focusRequester = FocusRequester()
    Box {
        Column {
            OutlinedTextField(
                value = convertToTitleCase(selectedColour),
                onValueChange = {
                    onSelectionChanged(it)
                },
                modifier = Modifier
                    .then(modifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.spinner_icon
                        ),
                        contentDescription = null
                    )
                },
                readOnly = true
            )
            DropdownMenu(
                modifier = Modifier
                    .background(Color.White)
                    .then(modifier),
                expanded = expanded,
                properties = PopupProperties(focusable = false),
                onDismissRequest = { expanded = false },
            ) {
                val colourNames = ArrayList<String>()
                otherSKUsByGroupKey.forEach { entry ->
                    val otherSKUList = otherSKUsByGroupKey[entry.key]
                    val isZeroQuantity = otherSKUList?.any {
                        (it.quantity == 0) // show colours which have zero quantity
                    }
                    if (isZeroQuantity == true) {
                        colourNames.add(entry.key)
                    }
                }
                colourNames.reverse()
                colourNames.forEach { colourName ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .border(width = 0.dp, color = Color(R.color.color_9D9D9D))
                            .background(Color.White),
                        onClick = {
                            selectedColour = colourName
                            expanded = false
                        },
                        text = {
                            Text(
                                text = (convertToTitleCase(colourName)),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .align(Alignment.Start),
                                color = Color.Black,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    fontFamily = FontFamily(Font(R.font.opensans_medium)),
                                    fontWeight = FontWeight(400),
                                    color = Color.Black
                                )
                            )
                        }
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(1.dp)
                .clickable(
                    onClick = { expanded = !expanded }
                )
        )
    }
}

@Composable
fun SpinnerSizeView(
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    selectedGroupKey: String?,
    modifier: Modifier = Modifier,
    preselectedSize: OtherSkus?,
    onSelectionChanged: (selectedSize: String) -> Unit
) {
    var preselectedSizeString = ""
    if (preselectedSize != null && preselectedSize.quantity == 0) {
        preselectedSizeString = preselectedSize.size.toString()
    } else {
        preselectedSizeString = ""
    }
    var selectedSize by remember { mutableStateOf(preselectedSizeString) }
    var expanded by remember { mutableStateOf(false) } // initial value

    Box {
        Column {
            OutlinedTextField(
                value = selectedSize,
                onValueChange = onSelectionChanged,
                modifier = modifier,
                placeholder = {
                    Text(
                        text = "Select a size",
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.opensans_medium)),
                            fontWeight = FontWeight(400),
                            color = Color(R.color.color_9D9D9D)
                        )
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.spinner_icon
                        ),
                        contentDescription = null
                    )
                },
                readOnly = true
            )
            DropdownMenu(
                modifier = Modifier
                    .background(Color.White)
                    .then(modifier),
                expanded = expanded,
                properties = PopupProperties(focusable = false),
                onDismissRequest = { expanded = false },
            ) {

                val otherSKUList = otherSKUsByGroupKey[selectedGroupKey]
                otherSKUList?.forEach { otherSKU ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .border(width = 0.dp, color = Color(R.color.color_9D9D9D))
                            .background(Color.White),
                        onClick = {
                            selectedSize = otherSKU.size.toString()
                            expanded = false
                            onSelectionChanged(selectedSize)
                        },
                        text = {
                            Text(
                                text = (otherSKU.size.toString()),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .align(Alignment.Start),
                                color = Color.Black,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp,
                                    fontFamily = FontFamily(Font(R.font.opensans_medium)),
                                    fontWeight = FontWeight(400),
                                    color = Color(R.color.black)
                                )
                            )
                        }
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .padding(1.dp)
                .clickable(
                    onClick = { expanded = !expanded }
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpinnerColourPreview() {
    MaterialTheme {
        SpinnerColourView(
            linkedMapOf(),
            "",
            modifier = Modifier
                .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                .fillMaxWidth(),
            preselectedColour = "White"
        ) { selectedColour -> /* do something with selected */ }
    }
}

@Preview(showBackground = true)
@Composable
fun BackInStockScreenPreview() {
    OneAppTheme {
        BackInStockScreen(
            modifier = Modifier,
            backToStockUiState = BackToStockUiState(),
            linkedMapOf(),
            selectedGroupKey = "selectedGroupKey",
            OtherSkus(),
            hasColor = true,
            hasSize = true
        ) { }
    }
}
