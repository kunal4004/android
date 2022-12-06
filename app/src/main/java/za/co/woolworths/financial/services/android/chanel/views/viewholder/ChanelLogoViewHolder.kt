package za.co.woolworths.financial.services.android.chanel.views.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelLogoViewBinding
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.DynamicBanner
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ImageManager

class ChanelLogoViewHolder(val binding: ChanelLogoViewBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(position: Int, list: List<DynamicBanner?>) {

        if (list.get(position)?.label.isNullOrEmpty() && list.get(position)?.externalImageRefV2.isNullOrEmpty()) {
            binding.tvLogoName.visibility = View.GONE
            binding.imgViewLogo.visibility = View.GONE
            return
        }
        if (list.get(position)?.label == null || list.get(position)?.label == AppConstant.EMPTY_STRING) {
            binding.tvLogoName.visibility = View.GONE
            binding.imgViewLogo.visibility = View.VISIBLE
            list.get(position)?.externalImageRefV2?.let {
                ImageManager.loadImage(binding.imgViewLogo,
                    it
                )
            }
        } else {
            binding.tvLogoName.visibility = View.VISIBLE
            binding.imgViewLogo.visibility = View.GONE
            binding.tvLogoName.text = list.get(position)?.label
        }
    }
}