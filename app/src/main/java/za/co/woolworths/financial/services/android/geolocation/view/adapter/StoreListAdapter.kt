package za.co.woolworths.financial.services.android.geolocation.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.store_row_header_layout.view.*
import kotlinx.android.synthetic.main.store_row_layout.view.*
import kotlinx.android.synthetic.main.store_row_layout.view.storeSelectorLayout
import za.co.woolworths.financial.services.android.common.changeMeterToKM
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.StoreListRow
import za.co.woolworths.financial.services.android.util.StoreUtils

class StoreListAdapter(
    val context: Context?,
    val storesList: List<StoreListRow>?,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lastSelectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val v = layoutInflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.store_row_layout -> SavedAddressViewHolder(v)
            R.layout.store_row_header_layout -> HeaderViewHolder(v)
            else -> SavedAddressViewHolder(v)

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = storesList?.get(position)
        when (holder) {
            is HeaderViewHolder -> holder.bindItems(item as StoreListRow.Header)
            is SavedAddressViewHolder -> holder.bindItems(item as StoreListRow.StoreRow, position)
        }

    }

    override fun getItemCount(): Int = storesList?.size!!


    override fun getItemViewType(position: Int) = when (storesList?.get(position)) {
        is StoreListRow.StoreRow -> R.layout.store_row_layout
        is StoreListRow.Header -> R.layout.store_row_header_layout
        else->{
            R.layout.store_row_layout

        }
    }


    inner class SavedAddressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(storeRow: StoreListRow.StoreRow, position: Int) {
            storeRow?.apply {
                if(store?.locationId != "" && store?.storeName?.contains(StoreUtils.PARGO, true) == false) {
                    var pargoStoreName = store.storeName
                    pargoStoreName= KotlinUtils.capitaliseFirstLetter(pargoStoreName).toString() +
                            StoreUtils.BULLET + StoreUtils.PARGO
                    itemView.tvAddressNickName?.text = pargoStoreName
                } else {
                    itemView.tvAddressNickName?.text = KotlinUtils.capitaliseFirstLetter(store?.storeName)
                }
                itemView?.tvAddress?.text = store.storeAddress
                itemView?.txtStoreDistance?.text = store.distance?.let { changeMeterToKM(it) }
                if (lastSelectedPosition == position) {
                    itemView?.imgAddressSelector?.isChecked = true
                    itemView?.storeSelectorLayout?.setBackgroundResource(R.drawable.bg_select_store)
                } else {
                    itemView?.imgAddressSelector?.isChecked = false
                    itemView?.storeSelectorLayout?.setBackgroundResource(R.color.white)
                }
                itemView?.storeSelectorLayout?.setOnClickListener {
                    if (store?.locationId != "" && !AppInstanceObject.get().featureWalkThrough.pargo_store) {
                        listener.onFirstTimePargo()
                    } else {
                        lastSelectedPosition = bindingAdapterPosition
                        notifyDataSetChanged()
                        listener.onStoreSelected(store)
                    }
                }

            }
        }
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(headerRow: StoreListRow.Header?) {
            itemView?.tvStoreHeader?.text = headerRow?.headerName
        }
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store?)
        fun onFirstTimePargo()
    }
}
