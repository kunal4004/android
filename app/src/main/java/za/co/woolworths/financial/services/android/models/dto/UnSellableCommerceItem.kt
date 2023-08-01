package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class UnSellableCommerceItem(val quantity: Int?, val productId: String?, val displayCategory: String?, val internalImageURL: String?, val catalogRefId: String, val commerceItemClassType: String?, val colour: String?, val detailPageURL: String?, val size: String?, val productVariant: String?, val price: Price, val externalImageRefV2: String?, val productDisplayName: String?, val fulfillerType: String?, val productType: String?) :Serializable