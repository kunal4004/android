package za.co.woolworths.financial.services.android.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.NutritionalTableItem
import kotlinx.android.synthetic.main.nutritional_info_table_item.view.*
import za.co.wigroup.androidutils.Util.dpToPx


class NutritionalInformationListAdapter : RecyclerView.Adapter<NutritionalInformationListAdapter.ViewHolder>() {

    var data: List<NutritionalTableItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.nutritional_info_table_item, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(data[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(item: NutritionalTableItem) {
            val nutritionalDescription = item.nutritionalDescription.trim()
            itemView.unit.text = item.nutritionalDescriptionValue + " " + item.nutritionalMeasurement
            itemView.description.apply {
                nutritionalDescription.let {
                    text = when (it.startsWith("of", true)) {
                        true -> {
                            setPadding(dpToPx(16), 0, 0, 0)
                            it.decapitalize()
                        }
                        false -> {
                            it
                        }
                    }
                }
            }
        }
    }

    fun updateData(data: List<NutritionalTableItem>) {
        this.data = data
        notifyDataSetChanged()
    }

}