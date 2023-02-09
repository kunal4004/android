package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductSizeSelectorListItemBinding
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsContract

class SizeAdapter(
    val context: Context,
    var dataList: ArrayList<OtherSkus>,
    var listener: ColorAndSizeListener
) : RecyclerView.Adapter<SizeAdapter.ViewHolder>() {

    init {
        dataList.map { it.quantity = 1 }
    }

    var selectedSize: OtherSkus? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProductSizeSelectorListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }


    inner class ViewHolder(val itemBinding: ProductSizeSelectorListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(otherSku: OtherSkus) {
            with(otherSku) {
                itemBinding.size.text = size
                itemBinding.size.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemBinding.size.setBackgroundResource(
                    if (selectedSize?.sku.equals(otherSku.sku))
                        R.drawable.product_available_size_selected_background
                    else
                        R.drawable.product_available_size_un_selected_background
                )
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
        this.dataList = dataList
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