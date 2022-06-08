package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.SupplyDetailItemBinding
import za.co.woolworths.financial.services.android.ui.extension.bindString

internal class CliSupplyDetailListAdapter(private val items: Pair<MutableList<Pair<Int, Int>>, MutableList<Pair<Int, Int>>>) :
    RecyclerView.Adapter<CliSupplyDetailListAdapter.SupplyDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplyDetailViewHolder {
        return SupplyDetailViewHolder(
            SupplyDetailItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SupplyDetailViewHolder, position: Int) {
        holder.bindItems(items.first[position], items.second[position])
    }

    override fun getItemCount(): Int = items.first.size

    inner class SupplyDetailViewHolder(private val itemBinding: SupplyDetailItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(info: Pair<Int, Int>, contentDesc: Pair<Int, Int>) {
            with(itemBinding) {
                titleTextView.text = bindString(info.first)
                descriptionTextView.text = bindString(info.second)

                titleTextView.contentDescription = bindString(contentDesc.first)
                descriptionTextView.contentDescription = bindString(contentDesc.second)
            }
        }
    }
}