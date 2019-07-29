package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.block_card_reason_row.view.*
import za.co.woolworths.financial.services.android.models.dto.npc.BlockReason

internal class BlockCardReasonAdapter(private val blockReason: MutableList<BlockReason>?, val onClickListener: (BlockReason) -> Unit)
    : RecyclerView.Adapter<BlockCardReasonAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.block_card_reason_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        blockReason?.get(position)?.apply {
            holder.bindItems(this)
            holder.itemView?.setOnClickListener {
                for (reason in blockReason) {
                    reason.rowIsSelected = false
                }
                rowIsSelected = true
                notifyDataSetChanged()
                onClickListener(this)
            }

            holder.itemView.imRadioButton.setImageResource(if (rowIsSelected) R.drawable.checked_item else R.drawable.uncheck_item)
        }
    }

    override fun getItemCount(): Int = blockReason?.size ?: 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(blockReason: BlockReason?) {
            blockReason?.value?.let { itemView.tvBlockReasonTitle.text = it }
        }

        fun clickEvent() {

        }
    }
}