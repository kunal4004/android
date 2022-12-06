package za.co.woolworths.financial.services.android.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.NutritionalInfoTableItemBinding
import za.co.wigroup.androidutils.Util.dpToPx
import za.co.woolworths.financial.services.android.models.dto.NutritionalTableItem


class NutritionalInformationListAdapter : RecyclerView.Adapter<NutritionalInformationListAdapter.ViewHolder>() {

    var data: List<NutritionalTableItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NutritionalInfoTableItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        holder.bind(data[position])
    }

    class ViewHolder(val itemBinding: NutritionalInfoTableItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: NutritionalTableItem) {
            itemBinding.apply {
                val nutritionalDescription = item.nutritionalDescription.trim()
                with(item.nutritionalDescriptionValue) {
                    unit.text = when (this) {
                        "-" -> this
                        else -> this + " " + item.nutritionalMeasurement
                    }
                }

                description.apply {
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
    }

    fun updateData(data: List<NutritionalTableItem>) {
        this.data = data
        notifyDataSetChanged()
    }

}