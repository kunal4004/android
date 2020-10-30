package za.co.woolworths.financial.services.android.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.province_and_suburb_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.util.DeliveryType

class SuburbListAdapter(private var suburbList: ArrayList<Suburb>, var listener: ISuburbSelector, var deliveryType: DeliveryType?) : RecyclerView.Adapter<SuburbListAdapter.SuburbViewHolder>(), Filterable {

    var checkedItemPosition = -1
    var suburbFilterList = ArrayList<Suburb>()

    init {
        suburbFilterList = suburbList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuburbViewHolder {
        return SuburbViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.province_and_suburb_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return suburbFilterList.size
    }

    override fun onBindViewHolder(holder: SuburbViewHolder, position: Int) {
        holder.bindItem(position)
    }


    inner class SuburbViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                suburbFilterList[position].let {
                    name.text = it.name
                    selector.isChecked = checkedItemPosition == position

                    when (deliveryType) {
                        DeliveryType.DELIVERY -> {
                            name.paintFlags = if (!it.suburbDeliverable) Paint.STRIKE_THRU_TEXT_FLAG else Paint.ANTI_ALIAS_FLAG
                            itemView.alpha = if (!it.suburbDeliverable) 0.5f else 1f
                        }
                        DeliveryType.STORE_PICKUP -> {
                            name.paintFlags = if (!it.storeDeliverable) Paint.STRIKE_THRU_TEXT_FLAG else Paint.ANTI_ALIAS_FLAG
                            itemView.alpha = if (!it.suburbDeliverable) 0.5f else 1f
                        }
                    }
                }
                setOnClickListener {
                    if (deliveryType == DeliveryType.DELIVERY) {
                        if (!suburbFilterList[position].suburbDeliverable) return@setOnClickListener
                    } else {
                        if (!suburbFilterList[position].storeDeliverable) return@setOnClickListener
                    }
                    checkedItemPosition = position
                    notifyDataSetChanged()
                    listener.onSuburbSelected(suburbFilterList[position])
                }
            }

        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                suburbFilterList = if (charSearch.isEmpty()) {
                    suburbList
                } else {
                    suburbList.filter { it.name.contains(charSearch, true) } as ArrayList<Suburb>
                }
                val filterResults = FilterResults()
                filterResults.values = suburbFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                suburbFilterList = results?.values as ArrayList<Suburb>
                notifyDataSetChanged()
            }

        }
    }

    interface ISuburbSelector {
        fun onSuburbSelected(suburb: Suburb)
    }

}