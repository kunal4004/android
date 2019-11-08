package za.co.woolworths.financial.services.android.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.woolworths.financial.services.android.ui.adapters.holder.ResendStoreCardOTPViewHolder

class ResendOTPAdapter(private val onClickListener: (Int) -> Unit) : RecyclerView.Adapter<ResendStoreCardOTPViewHolder>() {

    var listItem: MutableList<Triple<Int, Int, String>>? = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResendStoreCardOTPViewHolder = ResendStoreCardOTPViewHolder(parent)

    override fun getItemCount(): Int = listItem?.size ?: 0

    override fun onBindViewHolder(holder: ResendStoreCardOTPViewHolder, position: Int) {
        listItem?.get(position)?.apply { holder.setItem(this, onClickListener) }
    }

    fun setItem(list: MutableList<Triple<Int, Int, String>>) {
        this.listItem = list
        notifyDataSetChanged()
    }
}