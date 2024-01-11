package za.co.woolworths.financial.services.android.presentation.createlist

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.presentation.addtolist.components.CreateNewListState
import za.co.woolworths.financial.services.android.presentation.common.BlackButton
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.presentation.common.ProgressView
import za.co.woolworths.financial.services.android.presentation.common.UnderlineButton
import za.co.woolworths.financial.services.android.presentation.createlist.components.CreateListScreenEvent
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorD8D8D8
import za.co.woolworths.financial.services.android.ui.wfs.theme.ErrorLabel
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily

@ExperimentalComposeUiApi
@Composable
fun CreateListScreen(
    modifier: Modifier = Modifier,
    state: CreateNewListState = CreateNewListState(),
    onEvent: (event: CreateListScreenEvent) -> Unit
) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .then(modifier)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderView(
                modifier = Modifier.padding(bottom = 24.dp, top = 15.dp),
                headerViewState = HeaderViewState.HeaderStateType1(
                    title = state.title
                )
            ) {
                onEvent(CreateListScreenEvent.BackPressed)
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorD8D8D8)
            )
            var listName by rememberSaveable { mutableStateOf("") }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    factory = { context ->
                        EditText(context).apply {
                            hint = context.getString(R.string.hint_list_name)
                            setHintTextColor(ContextCompat.getColor(context, R.color.color_666666))
                            setTextSize(
                                TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(
                                    R.dimen.twenty_four_sp
                                )
                            )
                            isSingleLine = true
                            textAlignment = View.TEXT_ALIGNMENT_CENTER
                            imeOptions = EditorInfo.IME_ACTION_DONE
                            background = null
                            setOnEditorActionListener { _, _, _ ->
                                val imm =
                                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(windowToken, 0)

                                if (listName.isNotEmpty()) {
                                    onEvent(CreateListScreenEvent.CreateList(listName = listName))
                                }
                                return@setOnEditorActionListener true
                            }
                            val mgr =
                                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            mgr.showSoftInput(this, InputMethodManager.SHOW_FORCED)
                            requestFocus()
                        }
                    }
                ) {
                    it.afterTextChanged { newText ->
                        listName = newText
                    }
                }

                if(state.isError) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        text = state.errorMessage.ifEmpty { stringResource(id = state.errorMessageId) },
                        style = TextStyle(
                            fontFamily = OpenSansFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = ErrorLabel
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            BlackButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .height(50.dp),
                text = stringResource(id = R.string.create_list).uppercase(),
                enabled = listName.isNotEmpty()
            ) {
                onEvent(CreateListScreenEvent.CreateList(listName))
            }

            if (state.cancelText.isNotEmpty()) {
                UnderlineButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                        .height(50.dp),
                    text = state.cancelText
                ) {
                    onEvent(CreateListScreenEvent.CancelClick)
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        when {
            state.isLoading -> {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.8f))
                )

                ProgressView(modifier = Modifier.align(Alignment.Center))
            }
            else -> {}
        }
    }
}


@ExperimentalComposeUiApi
@Preview(showBackground = true)
@Composable
private fun CreateListScreenPreview() {
    OneAppTheme {
        CreateListScreen(
            state = CreateNewListState(
                isLoading = false,
                title = stringResource(id = R.string.shop_create_list),
                isError = true,
                errorMessageId = R.string.create_list_name_error
            )
        ) {}
    }
}