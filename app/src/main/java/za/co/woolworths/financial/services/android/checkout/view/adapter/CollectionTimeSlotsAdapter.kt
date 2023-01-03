package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WhereAreWeDeliveringItemsBinding
import za.co.woolworths.financial.services.android.checkout.service.network.Slot
import za.co.woolworths.financial.services.android.checkout.view.CollectionTimeSlotsListener

class CollectionTimeSlotsAdapter(val listener: CollectionTimeSlotsListener?) :
    RecyclerView.Adapter<CollectionTimeSlotsAdapter.CollectionTimeSlotViewHolder>() {
    private var list: ArrayList<Slot> = ArrayList(0)
    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CollectionTimeSlotViewHolder {
        return CollectionTimeSlotViewHolder(
            WhereAreWeDeliveringItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: CollectionTimeSlotViewHolder, position: Int) {
        if (list.size <= 0 || holder.adapterPosition >= list.size) {
            return
        }
        holder.bindItemView(holder.adapterPosition, list)
    }

    override fun getItemCount(): Int = list.size

    inner class CollectionTimeSlotViewHolder(val itemBinding: WhereAreWeDeliveringItemsBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItemView(adapterPosition: Int, list: ArrayList<Slot>) {
            val slot = list[adapterPosition]
            itemBinding.apply {
                titleTv?.text = slot.hourSlot
                titleTv?.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (selectedPosition == adapterPosition) {
                            R.color.white
                        } else {
                            R.color.color_444444
                        }
                    )
                )
                titleTv?.background = ContextCompat.getDrawable(
                    itemView.context,
                    if (selectedPosition == adapterPosition) {
                        R.drawable.checkout_delivering_title_round_button_pressed
                    } else {
                        R.drawable.checkout_delivering_title_round_button
                    }
                )
                itemView.setOnClickListener {
                    setSelectedItem(adapterPosition)
                }
            }
        }
    }

    internal fun setSelectedItem(adapterPosition: Int) {
        synchronized(this) {
            if (selectedPosition in 0 until list.size) {
                notifyItemChanged(selectedPosition, list[selectedPosition])
            }
            if (adapterPosition in 0 until list.size) {
                selectedPosition = adapterPosition
                notifyItemChanged(adapterPosition, list[adapterPosition])

                listener?.setSelectedTimeSlot(list[adapterPosition])
            }
        }
    }

    fun setCollectionTimeSlotData(list: ArrayList<Slot>?) {
        this.list = list ?: ArrayList(0)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
