package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelCategoryNavigationViewBinding
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.adapter.BrandLandingAdapter
import za.co.woolworths.financial.services.android.chanel.views.adapter.CategoryNavigationAdapter
import za.co.woolworths.financial.services.android.util.AppConstant

class ChanelCategoryNavigationViewHolder(
    val binding: ChanelCategoryNavigationViewBinding,
    val chanelNavigationClickListener: ChanelNavigationClickListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, list: List<DynamicBanner?>, context: Context?) {
        if(position >= list.size || position < 0){
            return
        }
        var bannerLabel: String? = AppConstant.EMPTY_STRING;
        var bannerImage: String? = AppConstant.EMPTY_STRING;

        for (banner in list) {
            if (banner?.name.equals(BrandLandingAdapter.LOGO, true)) {
                bannerLabel = banner?.label
                bannerImage = banner?.externalImageRefV2
            }
        }

        list[position]?.navigation?.let {
            val adapter = CategoryNavigationAdapter(context, it, chanelNavigationClickListener, bannerImage, bannerLabel)
            val itemDecor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

            binding.rvCategory.layoutManager = LinearLayoutManager(context)
            binding.rvCategory.adapter = adapter
            binding.rvCategory.addItemDecoration(itemDecor)
        }
    }
}