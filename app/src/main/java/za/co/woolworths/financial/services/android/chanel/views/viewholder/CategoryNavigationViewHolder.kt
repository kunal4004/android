package za.co.woolworths.financial.services.android.chanel.views.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelCatagoriesNavigationItemBinding

import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener

class CategoryNavigationViewHolder(
    val itemBinding: ChanelCatagoriesNavigationItemBinding,
    val chanelNavigationClickListener: ChanelNavigationClickListener
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(position: Int, list: List<Navigation>, bannerImage: String?, bannerLabel: String?) {
        if(position >= list.size || position < 0){
            return
        }
        itemBinding.rvCategoryName.text = list[position].displayName
        itemView.setOnClickListener {
            chanelNavigationClickListener.clickCategoryListViewCell(list[position], bannerImage, bannerLabel, true)
        }
    }
}