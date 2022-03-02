package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.store_row_layout.view.*
import za.co.woolworths.financial.services.android.geolocation.network.model.Store

class StoreListAdapter (
    val context: Context,
    val storeList: List<Store>?,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<StoreListAdapter.SavedAddressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedAddressViewHolder {
        return SavedAddressViewHolder(
            LayoutInflater.from(context).inflate(R.layout.store_row_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        storeList?.let {
            return it.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: SavedAddressViewHolder, position: Int) {
        holder.tvAddressNickname.text = storeList?.get(position)?.storeName
        holder.tvAddress.text = storeList?.get(position)?.storeAddress
        holder.tvStoreDistance.text = storeList?.get(position)?.distance.toString()
    }

    inner class SavedAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAddressNickname = itemView.tvAddressNickName
        val tvStoreDistance= itemView.txtStoreDistance
        val imgAddressSelector= itemView.imgAddressSelector
        val view = itemView
        val tvAddress = itemView.tvAddress
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store)
    }
}
