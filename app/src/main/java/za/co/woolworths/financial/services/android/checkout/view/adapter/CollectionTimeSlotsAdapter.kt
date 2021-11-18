package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.where_are_we_delivering_items.view.*
import za.co.woolworths.financial.services.android.checkout.service.network.Slot

class CollectionTimeSlotsAdapter :
    RecyclerView.Adapter<CollectionTimeSlotsAdapter.CollectionTimeSlotViewHolder>() {
    private var list: ArrayList<Slot> = ArrayList(0)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectionTimeSlotViewHolder {
        return CollectionTimeSlotViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.where_are_we_delivering_items, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CollectionTimeSlotViewHolder, position: Int) {
        if(list.size <= 0 || holder.adapterPosition >= list.size ){
            return
        }
        holder.bindItemView(holder.adapterPosition, list[holder.adapterPosition])
    }

    override fun getItemCount(): Int = list.size

    class CollectionTimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItemView(adapterPosition: Int, slot: Slot?) {
            itemView.apply {
                titleTv?.text = slot?.hourSlot
            }
        }
    }

    fun setCollectionTimeSlotData(list: ArrayList<Slot>?) {
        this.list = list ?: ArrayList(0)
        notifyDataSetChanged()
    }
}
