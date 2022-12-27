package za.co.woolworths.financial.services.android.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductSizeSelectorListItemBinding
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsContract

class ProductSizeSelectorAdapter(
    val context: Context,
    val dataList: ArrayList<OtherSkus>,
    var lowStock: Int,
    var listener: ProductDetailsContract.ProductDetailsView
) : RecyclerView.Adapter<ProductSizeSelectorAdapter.ViewHolder>() {

    var otherSkus: ArrayList<OtherSkus> = arrayListOf()

    init {
        otherSkus = dataList
    }

    var selectedSize: OtherSkus? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProductSizeSelectorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return otherSkus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(otherSkus[position])
    }


    inner class ViewHolder(val itemBinding: ProductSizeSelectorListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(otherSku: OtherSkus) {
            with(otherSku) {
                itemBinding.size.text = size
                when {
                    quantity == 0 -> {
                        itemBinding.size.setTextColor(ContextCompat.getColor(context, R.color.black))
                        itemBinding.size.setBackgroundResource(if (selectedSize?.sku.equals(otherSku.sku)) R.drawable.product_no_stock_size_selected_background else R.drawable.product_no_stock_size_un_selected_background)
                    }
                    lowStock > quantity && quantity != -1 && selectedSize?.sku.equals(otherSku.sku) -> {
                        itemBinding.size.setTextColor(ContextCompat.getColor(context,
                            R.color.low_stock_indicator))
                        itemBinding.size.setBackgroundResource(R.drawable.ic_rectangle_low_stock)
                    }
                    quantity == -1 -> {
                        itemBinding.size.setTextColor(ContextCompat.getColor(context, R.color.black))
                        itemBinding.size.setBackgroundResource(if (selectedSize?.sku.equals(otherSku.sku)) R.drawable.product_available_size_selected_background else R.drawable.product_available_size_un_selected_background)
                    }
                    else -> {
                        itemBinding.size.setTextColor(ContextCompat.getColor(context, R.color.black))
                        itemBinding.size.setBackgroundResource(if (selectedSize?.sku.equals(otherSku.sku)) R.drawable.product_available_size_selected_background else R.drawable.product_available_size_un_selected_background)
                    }
                }
            }
            itemBinding.root.setOnClickListener {
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