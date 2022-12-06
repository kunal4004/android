package za.co.woolworths.financial.services.android.chanel.views.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelAppBannerViewBinding
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.util.ImageManager

class ChanelAppBannerViewHolder(private val binding: ChanelAppBannerViewBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, list: List<DynamicBanner?>) {
        ImageManager.setPicture(binding.imgViewBanner, list.get(position)?.externalImageRefV2.toString())
    }
}