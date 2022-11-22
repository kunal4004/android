package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.store_row_layout.view.*
import za.co.woolworths.financial.services.android.common.changeMeterToKM
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.StoreUtils

class StoreListAdapter (
    val context: Context,
    val storeList: List<Store>?,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<StoreListAdapter.SavedAddressViewHolder>() {

    private var lastSelectedPosition: Int = -1

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
        holder.bindItems(storeList?.get(position), position)
    }

    inner class SavedAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(store: Store?, position: Int) {
            if(store?.locationId != "" && store?.storeName?.contains(StoreUtils.PARGO, true) == false) {
                var pargoStoreName = store.storeName
                pargoStoreName= " ${StoreUtils.PARGO} $pargoStoreName"
                itemView.tvAddressNickName.text = KotlinUtils.capitaliseFirstLetter(pargoStoreName)
            } else {
                itemView.tvAddressNickName.text = KotlinUtils.capitaliseFirstLetter(store?.storeName)
            }
            itemView.tvAddress.text = store?.storeAddress
            itemView.txtStoreDistance.text = store?.distance?.let { changeMeterToKM(it) }
            if (lastSelectedPosition == position) {
                itemView.imgAddressSelector?.isChecked = true
                itemView.storeSelectorLayout?.setBackgroundResource(R.drawable.bg_select_store)
            } else {
                itemView.imgAddressSelector?.isChecked = false
                itemView.storeSelectorLayout?.setBackgroundResource(R.color.white)
            }
            itemView.storeSelectorLayout?.setOnClickListener {
                if(store?.locationId != ""&&!AppInstanceObject.get().featureWalkThrough.pargo_store){
                    listener.onFirstTimePargo()
                }
                else {
                    lastSelectedPosition = adapterPosition
                    notifyDataSetChanged()
                    listener.onStoreSelected(store)
                }
            }
        }
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store?)
        fun onFirstTimePargo()
    }
}
