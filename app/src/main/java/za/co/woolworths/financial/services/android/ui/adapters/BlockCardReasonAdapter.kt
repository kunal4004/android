package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BlockCardReasonRowBinding
import za.co.woolworths.financial.services.android.models.dto.npc.BlockReason

internal class BlockCardReasonAdapter(private val blockReason: MutableList<BlockReason>?, val onClickListener: (BlockReason) -> Unit)
    : RecyclerView.Adapter<BlockCardReasonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BlockCardReasonRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        blockReason?.get(position)?.apply {
            holder.bindItems(this)
            holder.itemView.setOnClickListener {
                for (reason in blockReason) {
                    reason.rowIsSelected = false
                }
                rowIsSelected = true
                notifyDataSetChanged()
                onClickListener(this)
            }

            holder.itemBinding.imRadioButton.setImageResource(if (rowIsSelected) R.drawable.checked_item else R.drawable.uncheck_item)
        }
    }

    override fun getItemCount(): Int = blockReason?.size ?: 0

    class ViewHolder(val itemBinding: BlockCardReasonRowBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(blockReason: BlockReason?) {
            blockReason?.value?.let { itemBinding.tvBlockReasonTitle.text = it }
        }
    }
}