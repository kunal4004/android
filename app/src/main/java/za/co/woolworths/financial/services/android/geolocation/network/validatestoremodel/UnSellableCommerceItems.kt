package za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class UnSellableCommerceItems(

  var quantity: Int? = null,
  var productId: String? = null,
  var displayCategory: String? = null,
  var internalImageURL: String? = null,
  var catalogRefId: String? = null,
  var commerceItemClassType: String? = null,
  var colour: String? = null,
  var detailPageURL: String? = null,
  var size: String? = null,
  var productVariant: String? = null,
  var price: Price? = Price(),
  var externalImageURL: String? = null,
  var productDisplayName: String? = null,
  var fulfillerType: String? = null,
  var productType: String? = null,
  var externalImageRefV2: String? = null,

  ) : Parcelable