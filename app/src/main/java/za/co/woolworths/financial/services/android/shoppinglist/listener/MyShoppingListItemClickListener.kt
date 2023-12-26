package za.co.woolworths.financial.services.android.shoppinglist.listener

import za.co.woolworths.financial.services.android.shoppinglist.model.EditOptionType

interface MyShoppingListItemClickListener {
    fun itemEditOptionsClick(editOptionType: EditOptionType)
}