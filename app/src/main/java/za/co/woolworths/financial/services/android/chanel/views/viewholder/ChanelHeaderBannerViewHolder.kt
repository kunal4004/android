package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelHeaderBannerViewBinding


import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.chanel.views.adapter.ChanelHeaderBannerAdapter
import za.co.woolworths.financial.services.android.util.AppConstant


class ChanelHeaderBannerViewHolder(val binding: ChanelHeaderBannerViewBinding, val parent: ViewGroup) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        position: Int,
        list: List<DynamicBanner?>,
        context: Context?,
        chanelNavigationClickListener: ChanelNavigationClickListener
    ) {

        var bannerLabel: String? = AppConstant.EMPTY_STRING;
        var bannerImage: String? = AppConstant.EMPTY_STRING;

        for (banner in list) {
            if (banner?.name.equals(BrandLandingAdapter.LOGO, true)) {
                bannerLabel = banner?.label
                bannerImage = banner?.externalImageRefV2
            }
        }
        list[position]?.products?.let {
            val adapter =
                ChanelHeaderBannerAdapter(
                    context,
                    it, chanelNavigationClickListener,
                    bannerLabel,
                    bannerImage
                )
            val layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
            binding.tvHeaderLabel.text = list[position]?.headerText
            binding.rvProducts.layoutManager = layoutManager
            binding.rvProducts.adapter = adapter
        }
    }
}