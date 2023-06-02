package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.os.Parcelable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreNearbyItemBinding
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.util.WFormatter


class CardsOnMapAdapter(context: Activity, storeDetailsList: List<StoreDetails>) :
    PagerAdapter() {
    private var mContext: Activity
    private var storeDetailsList: List<StoreDetails>

    init {
        mContext = context
        this.storeDetailsList = storeDetailsList
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as? View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val binding:StoreNearbyItemBinding =
            StoreNearbyItemBinding.inflate(LayoutInflater.from(container.context), container, false)
        binding.storeName.text = storeDetailsList[position].name
        if (!TextUtils.isEmpty(storeDetailsList[position].address)) binding.storeAddress.text =
            storeDetailsList[position].address
        val mKmDistance: Int = mContext.resources.getDimensionPixelSize(R.dimen.distance_km)
        val ssDistance =
            SpannableString(WFormatter.formatMeter(storeDetailsList[position].distance))
        val mSpanKm = SpannableString(mContext.resources.getString(R.string.distance_in_km))
        mSpanKm.setSpan(AbsoluteSizeSpan(mKmDistance),
            0,
            mSpanKm.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        val mDistancekM = TextUtils.concat(ssDistance, "\n", mSpanKm)
        binding.distance.text = mDistancekM
        if (storeDetailsList[position].offerings != null) binding.offerings.text = WFormatter.formatOfferingString(
            storeDetailsList[position].offerings)
        if (storeDetailsList[position].times != null) {
            try {
                val mHour =
                    WFormatter.formatOpenUntilTime(storeDetailsList[position].times.getOrNull(0)?.hours)
                binding.timeing.text = mContext.resources.getString(R.string.open_until,mHour)
            } catch (ignored: ArrayIndexOutOfBoundsException) {
                // Do Nothing
            }
        }
        container.addView(binding.root)
        return binding.root
    }

    override fun getCount(): Int {
        return storeDetailsList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        super.restoreState(state, loader)
    }

    override fun saveState(): Parcelable? {
        return null
    }
}