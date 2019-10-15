package za.co.woolworths.financial.services.android.models.dto.temporary_store_card

data class StoreCard(val holderType: String?, val type: String?, val expiryDate:String?, val daysUntilExpiry: String?, val idRequired: Boolean?, val sequence: String, val embossedName: String?, val usage: String?, val blockCode: String, val number: String, val dateOpened: String, val dateLastMaintained: String)