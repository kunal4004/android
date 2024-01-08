package za.co.woolworths.financial.services.android.util

enum class ProductType(val value: String, val longHeader: String, val shortHeader: String) {
    DEFAULT("default", "YOUR GENERAL ITEM", "GENERAL"), HOME_COMMERCE_ITEM("homeCommerceItem", "YOUR HOME ITEM", "HOME"), CLOTHING_COMMERCE_ITEM("clothingCommerceItem", "YOUR CLOTHING ITEM", "CLOTHING"), PREMIUM_BRAND_COMMERCE_ITEM("premiumBrandCommerceItem", "YOUR PREMIUM BRAND ITEM", "PREMIUM BRAND"), FOOD_COMMERCE_ITEM("foodCommerceItem", "YOUR FOOD ITEM", "FOOD"), GIFT_COMMERCE_ITEM("gwpCommerceItem", "YOUR GIFT ITEM", "GIFT"),CONNECT_COMMERCE_ITEM("connectCommerceItem","YOUR WCONNECT ITEM","WCONNECT"), OTHER_ITEMS("", "YOUR OTHER ITEM", "OTHER")
}
