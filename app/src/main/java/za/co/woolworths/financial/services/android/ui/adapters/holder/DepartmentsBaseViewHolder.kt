package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class DepartmentsBaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(position: Int)
}