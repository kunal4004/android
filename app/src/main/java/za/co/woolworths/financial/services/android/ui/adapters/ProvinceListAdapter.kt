package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.province_and_suburb_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.Province

class ProvinceListAdapter(private var provinceList: ArrayList<Province>, var listener: IProvinceSelector) : RecyclerView.Adapter<ProvinceListAdapter.ProvinceViewHolder>() {

    var checkedItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProvinceListAdapter.ProvinceViewHolder {
        return ProvinceViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.province_and_suburb_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return provinceList.size
    }

    override fun onBindViewHolder(holder: ProvinceListAdapter.ProvinceViewHolder, position: Int) {
        holder.bindItem(position)
    }

    inner class ProvinceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {

                provinceList[position].let {
                    name.text = it.name
                    selector.isChecked = checkedItemPosition == position
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
