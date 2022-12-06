package za.co.woolworths.financial.services.android.util.expand

import android.view.View
import androidx.recyclerview.widget.RecyclerView

open class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var parentListItemExpandCollapseListener: ParentListItemExpandCollapseListener? = null
    var isRowExpanded = false

    interface ParentListItemExpandCollapseListener {
        fun onParentListItemExpanded(position: Int)
        fun onParentListItemCollapsed(position: Int)
        fun onParentListItemExpandedChanged(position: Int)
    }

    fun setMainItemClickToExpand() {
        itemView.setOnClickListener(this)
    }

    open fun onExpansionToggled(expanded: Boolean) {}

    open fun setExpanded(expanded: Boolean) {
        isRowExpanded = expanded
    }

    override fun onClick(v: View) {
        if (isRowExpanded) {
            collapseView()
        } else {
            expandView()
        }
    }

    fun shouldItemViewClickToggleExpansion(): Boolean {
        return true
    }

    fun expandView() {
        isRowExpanded = true
        onExpansionToggled(false)
        if (parentListItemExpandCollapseListener != null) {
            parentListItemExpandCollapseListener!!.onParentListItemExpanded(adapterPosition)
        }
    }

    fun collapseView() {
        isRowExpanded = false
        onExpansionToggled(true)
        if (parentListItemExpandCollapseListener != null) {
            parentListItemExpandCollapseListener!!.onParentListItemCollapsed(adapterPosition)
        }
    }

    protected fun expandView(notifyChange: Boolean) {
        parentListItemExpandCollapseListener!!.onParentListItemExpandedChanged(adapterPosition)
    }
}