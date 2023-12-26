package za.co.woolworths.financial.services.android.shoppinglist.component

sealed class MyListScreenEvents {

    object None: MyListScreenEvents()

    data class DismissDialog(
        val isSuccess: Boolean = false,
        val listName: String = ""
    ): MyListScreenEvents()
}
