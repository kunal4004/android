package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ApplyNowMainItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowSectionType
import za.co.woolworths.financial.services.android.models.dto.account.applynow.Children

class ApplyNowMainAdapter(var data: List<Children>) :
    RecyclerView.Adapter<ApplyNowMainAdapter.ApplyNowMainViewHolder>() {

    init {
        data.sortedBy { it.order }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplyNowMainViewHolder {
        return ApplyNowMainViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.apply_now_main_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ApplyNowMainViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }


    inner class ApplyNowMainViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ApplyNowMainItemBinding.bind(itemView)
        fun bind(item: Children) {
            with(binding) {
                tvApplyNowMainItem.text = item.title
                rcvApplyNowMainItem.adapter = when (ApplyNowSectionType.valueOf(item.type)) {
                    ApplyNowSectionType.LEFT_ICON_WITH_CONTENT_EXPANDABLE -> {
                        ApplyNowExpandableAdapter(item.children) }
                    else -> {
                        ApplyNowChildrensAdapter(
                            ApplyNowSectionType.valueOf(item.type),
                            item.children
                        )
                    }
                }
            }
        }
    }
}
