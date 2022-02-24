package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.Store

class StoreListAdapter (
    val context: Context,
    val storeList: ArrayList<Store>,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<StoreListAdapter.SavedAddressViewHolder>() {
    var selectedPosition = -1;
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedAddressViewHolder {
        return SavedAddressViewHolder(
            LayoutInflater.from(context).inflate(R.layout.store_row_layout, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 5//addressList.size
    }

    override fun onBindViewHolder(holder: SavedAddressViewHolder, position: Int) {
        /*holder.tvAddressNickname.text = addressList[position].nickname
        holder.rbAddressSelector.isChecked = selectedPosition == position
        holder.view.setOnClickListener {
            selectedPosition = position
            listener.onAddressSelected(addressList[position])
            notifyDataSetChanged()
        }
        if (addressList[position].verified) {
            holder.tvUpdateAddress.visibility = View.GONE
        } else {
            holder.tvUpdateAddress.visibility = View.VISIBLE
        }
        holder.tvAddress.text = addressList[position].address1*/
    }

    inner class SavedAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /*val tvAddressNickname = itemView.tvAddressNickName
        val view = itemView
        val tvAddress = itemView.tvAddress
        val tvUpdateAddress = itemView.tvUpdateAddress
        val rbAddressSelector = itemView.rbAddressSelector*/
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store)
    }
}