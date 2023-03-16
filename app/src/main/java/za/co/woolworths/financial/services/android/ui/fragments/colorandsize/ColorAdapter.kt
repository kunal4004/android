package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductColorSelectorListItemBinding
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.DrawImage
import java.util.Collections

class ColorAdapter(
    private val colorsList: List<OtherSkus>,
    private val listener: ColorAndSizeListener,
    val spanCount: Int,
    selectedSku: OtherSkus?
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var spanList: List<OtherSkus> = listOf()
    private var selectedColor: String? = selectedSku?.colour

    init {
        if(colorsList.contains(selectedSku)) {
            val index = colorsList.indexOf(selectedSku)
            Collections.swap(colorsList, 0, index)
        }
        spanList = getSpanList(spanCount)
    }

    private fun getSpanList(spanCount: Int): List<OtherSkus> {
        return  if (spanCount > colorsList.size) colorsList else colorsList.subList(0, spanCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ProductColorSelectorListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return spanList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(spanList[position])
    }

    inner class ViewHolder(val itemBinding: ProductColorSelectorListItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(colorItem: OtherSkus?) {
            itemBinding.apply {
                if (colorItem?.styleIdOnSale == true) {
                    saveLabelImage.visibility = View.VISIBLE
                } else
                    saveLabelImage.visibility = View.GONE
                root.setOnClickListener {
                    selectedColor = colorItem?.colour ?: ""
                    listener.onColorSelection(colorItem, false)
                    notifyDataSetChanged()
                }

                colorItem?.let {
                    setSelectedColorIcon(itemBinding.color, it.externalColourRef)
                    border.apply {
                        setBackgroundResource(
                            if (it.colour.equals(selectedColor, true))
                                R.drawable.product_color_selected_background
                            else
                                R.drawable.product_color_un_selected_background
                        )
                    }
                }
            }
        }
    }

    fun showMoreColors() {
        spanList = colorsList
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedColor = null
        notifyDataSetChanged()
    }

    fun setColorSelection(position: Int) {
        selectedColor = spanList[position].colour
        notifyDataSetChanged()
        listener.onColorSelection(spanList[position], true)
    }

    fun showLess() {
        spanList = getSpanList(spanCount = spanCount)
        notifyDataSetChanged()
    }

}

private fun setSelectedColorIcon(mImSelectedColor: WrapContentDraweeView, imageUrl: String?) {
    val drawImage = DrawImage(WoolworthsApplication.getAppContext())
    mImSelectedColor.imageAlpha = if (TextUtils.isEmpty(imageUrl)) 0 else 255
    drawImage.displayImage(mImSelectedColor, imageUrl)
}
