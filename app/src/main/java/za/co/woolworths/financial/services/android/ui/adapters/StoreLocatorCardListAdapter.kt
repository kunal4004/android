package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.StoreNearbyListCardItemBinding
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.adapters.holder.StoreLocatorCardListViewHolder

class StoreLocatorCardListAdapter(private val clickListener: (StoreDetails) -> Unit) : RecyclerView.Adapter<StoreLocatorCardListViewHolder>() {
    private var storeLocatorCards: List<StoreDetails>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreLocatorCardListViewHolder {
        return StoreLocatorCardListViewHolder(
            StoreNearbyListCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StoreLocatorCardListViewHolder, position: Int) {
        storeLocatorCards?.get(holder.adapterPosition)?.let { storeDetails -> holder.setItem(storeDetails, clickListener) }

    }

    fun setItem(storeDetails: List<StoreDetails>) {
        this.storeLocatorCards = storeDetails
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = storeLocatorCards?.size ?: 0

}
