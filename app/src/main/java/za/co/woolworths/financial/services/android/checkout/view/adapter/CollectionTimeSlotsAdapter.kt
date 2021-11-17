package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.where_are_we_delivering_items.view.*

class CollectionTimeSlotsAdapter :
    RecyclerView.Adapter<CollectionTimeSlotsAdapter.CollectionTimeSlotViewHolder>() {
    private var list: ArrayList<Any> = ArrayList(0)

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
        holder.bindItemView()
    }

    override fun getItemCount(): Int = 3

    class CollectionTimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItemView() {
            itemView.apply {
                titleTv?.text = "10AM-11AM"
            }
        }
    }

    fun setCollectionTimeSlotData(list: ArrayList<Any>?) {
        this.list = list ?: ArrayList(0)
        notifyDataSetChanged()
    }
}
