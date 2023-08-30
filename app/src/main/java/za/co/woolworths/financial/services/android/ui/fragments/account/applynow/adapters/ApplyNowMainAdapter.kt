package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ApplyNowMainItemBinding
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowSectionType
import za.co.woolworths.financial.services.android.models.dto.account.applynow.Children
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.UniqueIdentifiers
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.setContentDescription

class ApplyNowMainAdapter(var data: List<Children>) :
    RecyclerView.Adapter<ApplyNowMainAdapter.ApplyNowMainViewHolder>() {

    init {
        data = data.sortedBy { it.order }
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
                item.description?.apply {
                    tvDescApplyNowMainItem.visibility = View.VISIBLE
                    tvDescApplyNowMainItem.text = item.description
                }
                rcvApplyNowMainItem.adapter = when (ApplyNowSectionType.valueOf(item.type)) {
                    ApplyNowSectionType.LEFT_ICON_WITH_CONTENT_EXPANDABLE -> {
                        ApplyNowExpandableAdapter(item.children,item.title) }
                    else -> {
                        ApplyNowChildrensAdapter(
                            ApplyNowSectionType.valueOf(item.type),
                            item.children,
                            item.title
                        )
                    }
                }
                addUniqueLocators(item.title)
            }
        }
        private fun ApplyNowMainItemBinding.addUniqueLocators(mainID: String){
            tvApplyNowMainItem.setContentDescription(mainID, viewName = UniqueIdentifiers.Title)
            tvDescApplyNowMainItem.setContentDescription(mainID, viewName = UniqueIdentifiers.Description)
        }
    }
}