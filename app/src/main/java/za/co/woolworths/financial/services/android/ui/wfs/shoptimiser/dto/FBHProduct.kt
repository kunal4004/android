package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto

/**
 * Enum class representing different types of FBH products.
 * @param productType The type of FBH product.
 */
enum class FBHProduct(val productType: String) {
    HomeWareProducts("homewareProducts"),
    ClothingProducts("clothingProducts"),
    BeautyProducts("beautyProducts");

    companion object {

        /**
         * Checks if the user is browsing an FBH product based on the product type.
         * @param productType The product type to check.
         * @return `true` if the product type corresponds to any FBH product; otherwise, `false`.
         */
        fun isUserBrowsingFBHProduct(productType: String?): Boolean {
            return values().any { productType?.contains(it.productType, ignoreCase = true) == true }
        }

        /**
         * Retrieves the FBHProduct enum based on the product type.
         * @param productType The product type to match.
         * @return The corresponding FBHProduct enum value, or `null` if not found.
         */
        fun getFBHProduct(productType: String?): FBHProduct? {
            return FBHProduct.values().find { it.productType == productType }
        }
    }
}