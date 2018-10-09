package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class RefinementBaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(`object`: T)
}