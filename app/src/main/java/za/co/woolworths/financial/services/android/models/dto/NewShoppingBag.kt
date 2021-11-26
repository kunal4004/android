package za.co.woolworths.financial.services.android.models.dto

/**
 * Created by Kunal Uttarwar on 11/10/21.
 */
data class NewShoppingBag(val isEnabled: Boolean, val title: String, val description: String, val options: List<ShoppingBagsOptions>)