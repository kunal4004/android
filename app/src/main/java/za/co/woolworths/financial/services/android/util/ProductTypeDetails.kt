package za.co.woolworths.financial.services.android.util

enum class ProductTypeDetails(val value: String, val longHeader: String) {
    DEFAULT("default", "General Item"), HOME_COMMERCE_ITEM("home", "Home Item"), CLOTHING_COMMERCE_ITEM("clothing", "Clothing Item"), PREMIUM_BRAND_COMMERCE_ITEM("premium", "Premium Brand Item"), FOOD_COMMERCE_ITEM("food", "Food Item"), OTHER_ITEMS("", "Other Item")
}