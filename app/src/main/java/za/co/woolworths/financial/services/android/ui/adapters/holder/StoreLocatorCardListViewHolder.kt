package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.store_nearby_list_card_item.view.*
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.util.WFormatter

class StoreLocatorCardListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.store_nearby_list_card_item, parent, false))

    fun setItem(storeDetails: StoreDetails, clickListener: (StoreDetails) -> Unit) {
        with(storeDetails) {
            name?.let { name -> itemView.storeName?.text = name }
            address?.let { address -> itemView.storeAddress?.text = address }
            val distanceInKm = itemView.context?.resources?.getDimensionPixelSize(R.dimen.distance_km)
            val mSpanKm = SpannableString(itemView.context?.resources?.getString(R.string.distance_in_km))
            val ssDistance = SpannableString(WFormatter.formatMeter(distance))
            mSpanKm.setSpan(distanceInKm?.let { distance -> AbsoluteSizeSpan(distance) }, 0, mSpanKm.length, SPAN_INCLUSIVE_INCLUSIVE)
            itemView.distance?.text = TextUtils.concat(ssDistance, "\n", mSpanKm)
            itemView.offerings?.text = offerings?.let { offering -> WFormatter.formatOfferingString(offering) }
                    ?: ""
            val openingHour = times?.get(0)?.hours?.let { openHour -> WFormatter.formatOpenUntilTime(openHour) }
                    ?: ""
            itemView.timeing?.text = if (openingHour.isEmpty()) "" else "Open until $openingHour"
            itemView.setOnClickListener { clickListener(storeDetails) }
        }
    }
}