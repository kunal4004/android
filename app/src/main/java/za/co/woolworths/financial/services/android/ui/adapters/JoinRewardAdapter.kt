package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.ui.adapters.holder.JoinRewardViewHolder

class JoinRewardAdapter : RecyclerView.Adapter<JoinRewardViewHolder>() {
    var list: List<Triple<Int, Int, Int>>?= mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinRewardViewHolder {
        return JoinRewardViewHolder(parent)
    }

    override fun onBindViewHolder(holder: JoinRewardViewHolder, position: Int) {
        list?.get(position)?.apply { holder.bind(first, second, third) }
    }

    fun setItem(list: MutableList<Triple<Int, Int, Int>>) {
        this.list = list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = list?.size ?: 0

}
