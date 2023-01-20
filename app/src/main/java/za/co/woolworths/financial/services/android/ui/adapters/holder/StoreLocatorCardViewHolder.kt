package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.text.SpannableString
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreNearbyMapCardItemBinding
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.util.WFormatter

class StoreLocatorCardViewHolder(val itemBinding: StoreNearbyMapCardItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
    fun setItem(storeDetails: StoreDetails) {
        with(storeDetails) {
            name?.let { name -> itemBinding.storeName?.text = name }
            address?.let { address -> itemBinding.storeAddress?.text = address }
            val distanceInKm = itemBinding.root.context?.resources?.getDimensionPixelSize(R.dimen.distance_km)
            val mSpanKm = SpannableString(itemBinding.root.context?.resources?.getString(R.string.distance_in_km))
            val ssDistance = SpannableString(WFormatter.formatMeter(distance))
            mSpanKm.setSpan(distanceInKm?.let { distance -> AbsoluteSizeSpan(distance) }, 0, mSpanKm.length, SPAN_INCLUSIVE_INCLUSIVE)
            itemBinding.distance?.text = TextUtils.concat(ssDistance, "\n", mSpanKm)
            itemBinding.offerings?.text = offerings?.let { offering -> WFormatter.formatOfferingString(offering) }
                    ?: ""

            val openingHour = times?.get(0)?.hours?.let { openHour -> WFormatter.formatOpenUntilTime(openHour) }
                    ?: ""
            itemBinding.timeing?.text = if (openingHour.isEmpty()) "" else "Open until $openingHour"
        }
    }
}