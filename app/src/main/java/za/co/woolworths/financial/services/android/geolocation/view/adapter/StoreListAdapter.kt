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
import za.co.woolworths.financial.services.android.util.StoreListRow
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import com.awfs.coordination.databinding.StoreRowHeaderLayoutBinding

class StoreListAdapter(
    val context: Context?,
    val storesList: List<StoreListRow>?,
    val listener: OnStoreSelected
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var lastSelectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.store_row_layout -> SavedAddressViewHolder(
                StoreRowLayoutBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
            R.layout.store_row_header_layout -> HeaderViewHolder(
                StoreRowHeaderLayoutBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )
            else -> SavedAddressViewHolder(
                StoreRowLayoutBinding.inflate(
                    LayoutInflater.from(context),
                    parent,
                    false
                )
            )

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
        else -> {
            R.layout.store_row_layout

        }
    }


    inner class SavedAddressViewHolder(val itemBinding: StoreRowLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(storeRow: StoreListRow.StoreRow, position: Int) {
            itemBinding.apply {
                storeRow?.apply {
                        tvAddressNickName?.text =
                            KotlinUtils.capitaliseFirstLetter(store?.storeName)
                    tvAddress?.text = store.storeAddress
                    txtStoreDistance?.text = store.distance?.let { changeMeterToKM(it) }
                    if (lastSelectedPosition == position) {
                        imgAddressSelector?.isChecked = true
                        storeSelectorLayout?.setBackgroundResource(R.drawable.bg_select_store)
                    } else {
                        imgAddressSelector?.isChecked = false
                        storeSelectorLayout?.setBackgroundResource(R.color.white)
                    }
                    storeSelectorLayout?.setOnClickListener {
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
    }

    inner class HeaderViewHolder(val itemBinding: StoreRowHeaderLayoutBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(headerRow: StoreListRow.Header?) {
            itemBinding?.apply { tvStoreHeader?.text = headerRow?.headerName }

        }
    }

    interface OnStoreSelected {
        fun onStoreSelected(store: Store?)
        fun onFirstTimePargo()
    }
}
