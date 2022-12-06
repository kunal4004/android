package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.StoreNearbyMapCardItemBinding
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.adapters.holder.StoreLocatorCardViewHolder

class StoreLocatorCardAdapter : RecyclerView.Adapter<StoreLocatorCardViewHolder>() {
    private var storeLocatorCards: List<StoreDetails>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreLocatorCardViewHolder {
        return StoreLocatorCardViewHolder(
            StoreNearbyMapCardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: StoreLocatorCardViewHolder, position: Int) {
        storeLocatorCards?.get(position)?.let { storeDetails -> holder.setItem(storeDetails) }
    }

    fun setItem(storeDetails: List<StoreDetails>) {
        this.storeLocatorCards = storeDetails
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = storeLocatorCards?.size ?: 0

}
