package za.co.woolworths.financial.services.android.util

enum class ProductTypeDetails(val value: String, val longHeader: String, val shortHeader: String) {
    DEFAULT("default", "YOUR GENERAL ITEM", "GENERAL"), HOME_COMMERCE_ITEM("home", "YOUR HOME ITEM", "HOME"), CLOTHING_COMMERCE_ITEM("clothing", "YOUR CLOTHING ITEM", "CLOTHING"), PREMIUM_BRAND_COMMERCE_ITEM("premium", "YOUR PREMIUM BRAND ITEM", "PREMIUM BRAND"), FOOD_COMMERCE_ITEM("food", "YOUR FOOD ITEM", "FOOD"), GIFT_COMMERCE_ITEM("gwp", "YOUR GIFT ITEM", "GIFT"), OTHER_ITEMS("", "YOUR OTHER ITEM", "OTHER")
}