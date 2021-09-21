package za.co.woolworths.financial.services.android.util

enum class ProductTypeDetails(val value: String, val longHeader: String) {
    DEFAULT("default", "YOUR GENERAL ITEM"), HOME_COMMERCE_ITEM("home", "YOUR HOME ITEM"), CLOTHING_COMMERCE_ITEM("clothing", "YOUR CLOTHING ITEM"), PREMIUM_BRAND_COMMERCE_ITEM("premium", "YOUR PREMIUM BRAND ITEM"), FOOD_COMMERCE_ITEM("food", "YOUR FOOD ITEM"), OTHER_ITEMS("", "YOUR OTHER ITEM")
}