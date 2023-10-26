package za.co.woolworths.financial.services.android.shoppinglist.model

sealed class EditOptionType {
    object RemoveItemFromList : EditOptionType()
    data class CopyItemFromList(var list:List<String>): EditOptionType()
    object MoveItemFromList : EditOptionType()
}