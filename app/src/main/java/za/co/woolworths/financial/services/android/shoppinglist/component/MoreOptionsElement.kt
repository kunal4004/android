package za.co.woolworths.financial.services.android.shoppinglist.component

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MoreOptionsElement(
    val img:Int,
    val title:String
): Parcelable
