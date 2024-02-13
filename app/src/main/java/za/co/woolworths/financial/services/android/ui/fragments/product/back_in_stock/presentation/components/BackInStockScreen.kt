package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.presentation.common.ProgressView
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel.NotifyBackInStockViewModel.BackToStockUiState
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Composable
fun BackInStockScreen(
    modifier: Modifier = Modifier,
    backToStockUiState: BackToStockUiState,
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
                //  selectedGroupKey,
                //  selectedSku,
                hasColor,
                hasSize
            ) { backInStockEvents ->
                onEvent(backInStockEvents)
            }
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
                enabled = backToStockUiState.isColourSizeEmailSelected
            ) {
                onEvent(BackInStockScreenEvents.ConfirmClick)
            }
        }
    }

}

@Composable
private fun AddBISView(
    modifier: Modifier = Modifier,
    backToStockUiState: BackToStockUiState,
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    //selectedGroupKey: String?,
    //selectedSku: OtherSkus?,
    hasColor: Boolean,
    hasSize: Boolean,
    onEvent: (event: BackInStockScreenEvents) -> Unit
) {
    Column {
        Text(
            text = stringResource(id = R.string.bis_title),

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
            text = stringResource(id = R.string.bis_desc),

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
                text = stringResource(id = R.string.select_color),

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

            val selectedGroupKey = backToStockUiState.selectedGroupKey
            if (selectedGroupKey != null) {
                SpinnerColourView(
                    otherSKUsByGroupKey,
                    selectedGroupKey,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                        .fillMaxWidth(),
                    preselectedColour = selectedGroupKey
                ) { selectedColour ->
                    onEvent(BackInStockScreenEvents.OnColorSelected(selectedColour))
                }
            }
        }

        if (hasSize) {
            Text(
                text = stringResource(id = R.string.select_size),

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
            val selectedGroupKey = backToStockUiState.selectedGroupKey
            val selectedSku = backToStockUiState.selectedSku
            otherSKUsByGroupKey[selectedGroupKey]?.let { otherSKUList ->
                val zeroQuantityList = ArrayList<OtherSkus>()
                otherSKUList.forEach { otherSKU ->
                    if (otherSKU.quantity == 0) {
                        zeroQuantityList.add(otherSKU)
                    }
                }
                // remove available item list from map and keep only non available items in map
                // to display non available items only
                otherSKUsByGroupKey.remove(selectedGroupKey)?.let {
                    otherSKUsByGroupKey.put(selectedGroupKey.toString(), zeroQuantityList)
                }
            }
            SpinnerSizeView(
                otherSKUsByGroupKey,
                selectedGroupKey,
                modifier = Modifier
                    .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                    .fillMaxWidth(),
                preselectedOtherSkus = selectedSku,
                onSelectionChanged = { selectedSize ->
                    onEvent(BackInStockScreenEvents.OnSizeSelected(selectedSize))
                },
                onOtherSkusChanged = { otherSkus ->
                    onEvent(BackInStockScreenEvents.OnOtherSKusSelected(otherSkus))
                }
            )
        }
        Text(
            text = stringResource(id = R.string.email_address),

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

        var textEmailAddress by remember { mutableStateOf(AppInstanceObject.getCurrentUsersID()) }
        onEvent(BackInStockScreenEvents.OnEmailChanged(textEmailAddress))

        val customTextSelectionColors = TextSelectionColors(
            handleColor = colorResource(id = R.color.color_007778),
            backgroundColor = colorResource(id = R.color.color_95C6C7)
        )
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            TextField(
                value = textEmailAddress,
                onValueChange = { afterTextChanged ->
                    textEmailAddress = afterTextChanged
                    onEvent(BackInStockScreenEvents.OnEmailChanged(textEmailAddress))
                },
                readOnly = false,
                enabled = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 0.dp)
                    .border(width = 1.dp, color = colorResource(R.color.color_EEEEEE))
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
    onSelectionChanged(selectedColour)

    Box {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {

            TextField(
                value = convertToTitleCase(selectedColour),
                onValueChange = onSelectionChanged,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .border(width = 1.dp, color = colorResource(R.color.color_EEEEEE)),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.placeholder_select_color),
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.opensans_medium)),
                            fontWeight = FontWeight(400),
                            color = colorResource(R.color.color_9D9D9D)
                        )
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.spinner_icon
                        ),
                        tint = Color.Black,
                        contentDescription = stringResource(id = R.string.c_description)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                readOnly = true
            )
            DropdownMenu(
                modifier = Modifier
                    .exposedDropdownSize()
                    .background(Color.White),
                expanded = expanded,
                properties = PopupProperties(focusable = false),
                onDismissRequest = { expanded = false }) {
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
                            .fillMaxWidth()
                            .border(width = 0.5.dp, color = colorResource(R.color.color_EEEEEE))
                            .background(Color.White),
                        // interactionSource = NoRippleInteractionSource(),
                        onClick = {
                            selectedColour = colourName
                            expanded = false
                            onSelectionChanged(selectedColour)
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinnerSizeView(
    otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>,
    selectedGroupKey: String?,
    modifier: Modifier = Modifier,
    preselectedOtherSkus: OtherSkus?,
    onSelectionChanged: (selectedSize: String) -> Unit,
    onOtherSkusChanged: (otherSkus: OtherSkus) -> Unit
) {
    var preselectedSizeString = ""
    if (preselectedOtherSkus != null && preselectedOtherSkus.quantity == 0) {
        preselectedSizeString = preselectedOtherSkus.size.toString()
    } else {
        preselectedSizeString = ""
    }
    var selectedSize by remember { mutableStateOf(preselectedSizeString) }
    var expanded by remember { mutableStateOf(false) } // initial value
    onSelectionChanged(selectedSize)

    Box {
        ExposedDropdownMenuBox(
            modifier = modifier,
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedSize,
                onValueChange = onSelectionChanged,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .border(width = 1.dp, color = colorResource(R.color.color_EEEEEE)),
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.placeholder_select_size),
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontFamily = FontFamily(Font(R.font.opensans_medium)),
                            fontWeight = FontWeight(400),
                            color = colorResource(R.color.color_9D9D9D)
                        )
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.spinner_icon
                        ),
                        tint = Color.Black,
                        contentDescription = stringResource(id = R.string.c_description)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                readOnly = true
            )
            DropdownMenu(
                modifier = Modifier
                    .exposedDropdownSize()
                    .background(Color.White),
                properties = PopupProperties(focusable = false),
                expanded = expanded,
                onDismissRequest = { expanded = false }) {

                val otherSKUList = otherSKUsByGroupKey[selectedGroupKey]
                otherSKUList?.forEach { otherSKU ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 0.5.dp, color = colorResource(R.color.color_EEEEEE))
                            .background(Color.White),
                        //   interactionSource = NoRippleInteractionSource(),
                        onClick = {
                            selectedSize = otherSKU.size.toString()
                            expanded = false
                            onSelectionChanged(selectedSize)
                            onOtherSkusChanged(otherSKU)
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
                                    color = colorResource(R.color.black)
                                )
                            )
                        }
                    )
                }
            }
        }
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

@Composable
fun showProgressDialog(
    backToStockUiState: BackToStockUiState,
    onEvent: (event: BackInStockScreenEvents) -> Unit
) {
    var showDialog by remember { mutableStateOf(backToStockUiState.isLoading) }

    Dialog(
        onDismissRequest = {
            showDialog = false
            onEvent(BackInStockScreenEvents.CancelClick)
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White)
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
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {

                ProgressView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun showSuccessDialog(
    backToStockUiState: BackToStockUiState,
    onEvent: (event: BackInStockScreenEvents) -> Unit
) {
    var showDialog by remember { mutableStateOf(backToStockUiState.isSuccess) }

    Dialog(
        onDismissRequest = {
            showDialog = false
            onEvent(BackInStockScreenEvents.CancelClick)
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color.White)
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
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.loader),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 5.dp)
                            .clickable {},
                        contentDescription = stringResource(id = R.string.c_description)
                    )
                    Text(
                        modifier = Modifier.padding(24.dp),
                        text = stringResource(id = R.string.notify_me_success_message),
                        style = TextStyle(
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            fontFamily = FontFamily(Font(R.font.futura_semi_bold)),
                            fontWeight = FontWeight(600),
                            color = Color(0xFF000000),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }
    }
}
