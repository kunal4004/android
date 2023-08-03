package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductColorSelectorListItemBinding
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsContract
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.DrawImage
import java.util.*

class ProductColorSelectorAdapter(val otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>, var listener: ProductDetailsContract.ProductDetailsView, spanCount: Int, selectedGroupKey: String?) : RecyclerView.Adapter<ProductColorSelectorAdapter.ViewHolder>() {

    private var selectedColor: String? = null
    private var colorsList: List<String> = arrayListOf()
    private var groupKeys: List<String> = arrayListOf()

    init {
        groupKeys = otherSKUsByGroupKey.keys.toList()

        selectedGroupKey?.apply {
            if (groupKeys.size > 1) {
                (0..groupKeys.indexOf(selectedGroupKey)).forEach {
                    Collections.swap(groupKeys, 0, it)
                }
            }
            selectedColor = selectedGroupKey
        }

        colorsList = groupKeys.take(spanCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProductColorSelectorListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return colorsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colorsList[position])
    }


    inner class ViewHolder(val itemBinding: ProductColorSelectorListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(color: String?) {
            itemBinding.apply {
                if (otherSKUsByGroupKey[color]?.getOrNull(0)?.styleIdOnSale == true) {
                    saveLabelImage.visibility = View.VISIBLE
                } else
                    saveLabelImage.visibility = View.GONE
                root.setOnClickListener {
                    selectedColor = color
                    listener.onColorSelection(selectedColor, false)
                    notifyDataSetChanged()
                }

                color?.let {
                    setSelectedColorIcon(
                        itemBinding.color,
                        otherSKUsByGroupKey[color]?.get(0)?.externalColourRef
                    )
                    border.apply {
                        setBackgroundResource(
                            if (it.equals(
                                    selectedColor,
                                    true
                                )
                            ) R.drawable.product_color_selected_background else R.drawable.product_color_un_selected_background
                        )
                    }
                }
            }
        }
    }

    fun showMoreColors() {
        colorsList = groupKeys
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedColor = null
        notifyDataSetChanged()
    }
    fun setColorSelection(position : Int){
        selectedColor = colorsList[position]
        notifyDataSetChanged()
        listener.onColorSelection(selectedColor,true)
    }

}

private fun setSelectedColorIcon(mImSelectedColor: WrapContentDraweeView, imageUrl: String?) {
    val drawImage = DrawImage(WoolworthsApplication.getAppContext())
    mImSelectedColor.imageAlpha = if (TextUtils.isEmpty(imageUrl)) 0 else 255
    drawImage.displayImage(mImSelectedColor, imageUrl)
}
