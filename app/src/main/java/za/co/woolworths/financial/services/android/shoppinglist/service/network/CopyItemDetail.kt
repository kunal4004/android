package za.co.woolworths.financial.services.android.shoppinglist.service.network

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CopyItemDetail (
    var skuID:String ,
    var catalogRefId:String,
    var quantity:String
)
