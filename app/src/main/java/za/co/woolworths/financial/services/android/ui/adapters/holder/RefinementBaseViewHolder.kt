package za.co.woolworths.financial.services.android.ui.adapters.holder

import androidx.recyclerview.widget.RecyclerView
import android.view.View

abstract class RefinementBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(position: Int)
}