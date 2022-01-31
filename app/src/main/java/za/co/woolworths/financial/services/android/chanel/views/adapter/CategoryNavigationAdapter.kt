package za.co.woolworths.financial.services.android.chanel.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.chanel.model.Navigation
import za.co.woolworths.financial.services.android.chanel.views.viewholder.CategoryNavigationViewHolder

class CategoryNavigationAdapter(val context: Context?, val list: List<Navigation>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CategoryNavigationViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chanel_catagories_navigation_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CategoryNavigationViewHolder) {
            holder.bind(position, list)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}