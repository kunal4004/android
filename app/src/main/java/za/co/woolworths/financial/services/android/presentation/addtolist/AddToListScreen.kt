package za.co.woolworths.financial.services.android.presentation.addtolist

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListScreenEvents
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListUiState
import za.co.woolworths.financial.services.android.presentation.common.HeaderView
import za.co.woolworths.financial.services.android.presentation.common.HeaderViewState
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color666666
import za.co.woolworths.financial.services.android.ui.wfs.theme.ColorEEEEEE
import za.co.woolworths.financial.services.android.ui.wfs.theme.FuturaFontFamily
import za.co.woolworths.financial.services.android.ui.wfs.theme.OneAppTheme

@Composable
fun AddToListScreen(
    modifier: Modifier = Modifier,
    listUiState: AddToListUiState = AddToListUiState(),
    onEvent: (event: AddToListScreenEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp)
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {

        val isError = !listUiState.isLoading && (listUiState.isError || listUiState.list.isEmpty())

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            HeaderView(
                modifier = Modifier.padding(top = 20.dp, bottom = 24.dp),
                headerViewState = HeaderViewState.HeaderStateType2(
                    title = stringResource(id = R.string.add_to_list)
                )
            ){
                onEvent(AddToListScreenEvents.CreateListClick)
            }

            if (isError) {
                ErrorScreen(
                    modifier = Modifier
                        .heightIn(max = 519.dp)
                        .background(ColorEEEEEE),
                    title = stringResource(id = R.string.oops_something_went_wrong),
                    desc = stringResource(id = R.string.no_connection_desc_1),
                    btnText = stringResource(id = R.string.retry)
                ) {
                    onEvent(AddToListScreenEvents.RetryClick)
                }
            } else {

                AddToListView(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White),
                    list = listUiState.list,
                    selectedItemsList = listUiState.selectedListItem
                ) {
                    onEvent(AddToListScreenEvents.OnItemClick(it))
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp)
                        .height(50.dp),
                    onClick = { onEvent(AddToListScreenEvents.ConfirmClick) },
                    colors = ButtonDefaults.buttonColors(Color.Black),
                    shape = RectangleShape,
                    enabled = listUiState.selectedListItem.isNotEmpty()
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm).uppercase(),
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        letterSpacing = 1.5.sp
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 8.dp)
                        .height(50.dp),
                    onClick = { onEvent(AddToListScreenEvents.CancelClick) },
                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                    shape = RectangleShape
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel)
                            .uppercase(),
                        style = TextStyle(
                            fontFamily = FuturaFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color666666,
                            textDecoration = TextDecoration.Underline
                        ),
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        if (listUiState.isLoading) {
            CircularProgressIndicator(
                color = Color.Black
            )
        }
    }
}

@Composable
private fun AddToListView(
    modifier: Modifier = Modifier,
    list: List<ShoppingList> = emptyList(),
    selectedItemsList: List<ShoppingList> = emptyList(),
    onListItemClick: (item: ShoppingList) -> Unit
) {
    Spacer(
        modifier = Modifier
            .height(1.dp)
            .fillMaxWidth()
            .background(colorResource(id = R.color.color_D8D8D8))
    )

    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState()
    ) {
        items(list, key = { item ->
            item.listId + "" // Return a stable + unique key for the item
        }) { item ->

            SingleLabelCheckBox(item, selectedItemsList) {
                onListItemClick(it)
            }
            Divider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = colorResource(id = R.color.color_D8D8D8)
            )
        }
    }
}

@Composable
fun SingleLabelCheckBox(
    item: ShoppingList,
    selectedListItems: List<ShoppingList> = emptyList(),
    onItemClick: (item: ShoppingList) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick(item)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            text = item.listName.uppercase(),
            style = TextStyle(
                fontFamily = FuturaFontFamily,
                fontWeight = FontWeight.W300,
                fontSize = 14.sp,
                color = Color.Black
            ),
            letterSpacing = 2.sp
        )

        Image(
            modifier = Modifier.padding(24.dp),
            painter = painterResource(
                id = if (selectedListItems.contains(item))
                    R.drawable.check_mark_icon
                else
                    R.drawable.uncheck_item
            ),
            contentDescription = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleLabelCheckBoxPreview() {
    OneAppTheme {
        SingleLabelCheckBox(
            item = ShoppingList(listId = "1", listName = "Nikesh"),
            selectedListItems = listOf(
                ShoppingList(listId = "1", listName = "Nikesh")
            )
        ) {}
    }

}

@Preview(showBackground = true)
@Composable
fun AddToListScreenPreview() {
    OneAppTheme {
        AddToListScreen(
            modifier = Modifier
                .background(Color.White),
            listUiState = AddToListUiState(
                isLoading = false,
                isError = false,
                list = listOf(
                    ShoppingList(
                        listId = "1",
                        listName = "Favourites"
                    ),
                    ShoppingList(
                        listId = "2",
                        listName = "Healthy Foods"
                    ),
                    ShoppingList(
                        listId = "3",
                        listName = "Healthy Foods 1"
                    ),
                    ShoppingList(
                        listId = "4",
                        listName = "Healthy Foods 2"
                    )
                ),
                selectedListItem = listOf(
                    ShoppingList(
                        listId = "1",
                        listName = "Favourites"
                    ),
                    ShoppingList(
                        listId = "4",
                        listName = "Healthy Foods 2"
                    )
                )
            )
        ) {}
    }
}

