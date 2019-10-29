package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_size_selector_list_item.view.*
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsContract
import java.util.*

class ProductSizeSelectorAdapter(val dataList: ArrayList<OtherSkus>, var listener: ProductDetailsContract.ProductDetailsView) : RecyclerView.Adapter<ProductSizeSelectorAdapter.ViewHolder>() {

    var otherSkus: ArrayList<OtherSkus> = arrayListOf()

    init {
        otherSkus = dataList
    }

    var selectedSize: OtherSkus? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.product_size_selector_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return otherSkus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(otherSkus[position])
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(otherSku: OtherSkus) {
            with(otherSku) {
                itemView.size.text = size
                when (quantity) {
                    0 -> {
                        itemView.sizeBackground.visibility = View.VISIBLE
                        itemView.size.setBackgroundResource(if (selectedSize?.sku.equals(otherSku.sku)) R.drawable.product_no_stock_size_selected_background else R.drawable.product_no_stock_size_un_selected_background)
                    }
                    else -> {
                        itemView.sizeBackground.visibility = View.INVISIBLE
                        itemView.size.setBackgroundResource(if (selectedSize?.sku.equals(otherSku.sku)) R.drawable.product_available_size_selected_background else R.drawable.product_available_size_un_selected_background)
                    }
                }
            }
            itemView.setOnClickListener {
                selectedSize = otherSku
                selectedSize?.let {
                    listener.onSizeSelection(it)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun updatedSizes(dataList: ArrayList<OtherSkus>) {
        clearSelection()
        otherSkus = dataList
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedSize = null
        notifyDataSetChanged()
    }

    fun setSelection(otherSku: OtherSkus?) {
        selectedSize = otherSku
        notifyDataSetChanged()
    }
}