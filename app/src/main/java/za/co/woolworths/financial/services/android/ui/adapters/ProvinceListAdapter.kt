package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ProvinceAndSuburbListItemBinding
import za.co.woolworths.financial.services.android.models.dto.Province

class ProvinceListAdapter(private var provinceList: ArrayList<Province>, var listener: IProvinceSelector) : RecyclerView.Adapter<ProvinceListAdapter.ProvinceViewHolder>() {

    var checkedItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceListAdapter.ProvinceViewHolder {
        return ProvinceViewHolder(
            ProvinceAndSuburbListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return provinceList.size
    }

    override fun onBindViewHolder(holder: ProvinceListAdapter.ProvinceViewHolder, position: Int) {
        holder.bindItem(position)
    }

    inner class ProvinceViewHolder(val itemBinding: ProvinceAndSuburbListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(position: Int) {
            itemView.apply {

                provinceList[position].let {
                    itemBinding.name.text = it.name
                    itemBinding.selector.isChecked = checkedItemPosition == position
                }

                setOnClickListener {
                    checkedItemPosition = position
                    notifyDataSetChanged()
                    listener.onProvinceSelected(provinceList[position])
                }
            }

        }
    }

    interface IProvinceSelector {
        fun onProvinceSelected(province: Province)
    }
}
