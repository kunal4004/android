package za.co.woolworths.financial.services.android.chanel.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ChanelCatagoriesNavigationItemBinding
import za.co.woolworths.financial.services.android.models.dto.brandlandingpage.Navigation
import za.co.woolworths.financial.services.android.chanel.views.ChanelNavigationClickListener
import za.co.woolworths.financial.services.android.chanel.views.viewholder.CategoryNavigationViewHolder

class CategoryNavigationAdapter(
    val context: Context?,
    val list: List<Navigation>,
    val chanelNavigationClickListener: ChanelNavigationClickListener,
    val bannerImage: String?,
    val bannerLabel: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryNavigationViewHolder(
            ChanelCatagoriesNavigationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            chanelNavigationClickListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryNavigationViewHolder) {
            holder.bind(position, list, bannerImage, bannerLabel)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}