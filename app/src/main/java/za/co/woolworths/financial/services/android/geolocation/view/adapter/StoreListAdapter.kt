package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.StoreRowLayoutBinding
import za.co.woolworths.financial.services.android.common.changeMeterToKM
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.util.KotlinUtils

class StoreListAdapter (
    val context: Context,
    val storeList: List<Store>?,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<StoreListAdapter.SavedAddressViewHolder>() {

    private var lastSelectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedAddressViewHolder {
        return SavedAddressViewHolder(
            StoreRowLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        storeList?.let {
            return it.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: SavedAddressViewHolder, position: Int) {
        holder.bindItems(storeList?.get(position), position)
    }

    inner class SavedAddressViewHolder(val itemBinding: StoreRowLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(store: Store?, position: Int) {
            itemBinding.apply {
                tvAddressNickName.text =
                    KotlinUtils.capitaliseFirstLetter(store?.storeName)
                tvAddress.text = store?.storeAddress
                txtStoreDistance.text = store?.distance?.let { changeMeterToKM(it) }
                if (lastSelectedPosition == position) {
                    imgAddressSelector?.isChecked = true
                    storeSelectorLayout?.setBackgroundResource(R.drawable.bg_select_store)
                } else {
                    imgAddressSelector?.isChecked = false
                    storeSelectorLayout?.setBackgroundResource(R.color.white)
                }
                storeSelectorLayout?.setOnClickListener {
                    lastSelectedPosition = adapterPosition
                    notifyDataSetChanged()
                    listener.onStoreSelected(store)
                }
            }
        }
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store?)
    }
}
