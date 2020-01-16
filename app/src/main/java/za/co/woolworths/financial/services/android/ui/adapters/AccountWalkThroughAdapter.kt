package za.co.woolworths.financial.services.android.ui.adapters

import android.content.res.TypedArray
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.ui.adapters.holder.AccountWalkThroughViewHolder

class AccountWalkThroughAdapter : RecyclerView.Adapter<AccountWalkThroughViewHolder>() {
    private var walkThroughItems: Triple<Array<String>?, TypedArray?, Array<String>?>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountWalkThroughViewHolder {
        return AccountWalkThroughViewHolder(parent)
    }

    override fun onBindViewHolder(holder: AccountWalkThroughViewHolder, position: Int) {
        holder.bind(walkThroughItems)
    }

    fun setItem(walkThroughItems: Triple<Array<String>?, TypedArray?, Array<String>?>?) {
        this.walkThroughItems = walkThroughItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = walkThroughItems?.first?.size ?: 0
}
