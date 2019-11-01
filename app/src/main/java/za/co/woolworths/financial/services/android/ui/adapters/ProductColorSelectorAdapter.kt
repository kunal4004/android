package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_color_selector_list_item.view.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsContract
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.DrawImage
import java.util.*


class ProductColorSelectorAdapter(val otherSKUsByGroupKey: HashMap<String, ArrayList<OtherSkus>>, var listener: ProductDetailsContract.ProductDetailsView) : RecyclerView.Adapter<ProductColorSelectorAdapter.ViewHolder>() {

    private var selectedColor: String? = null
    private var colorsList: List<String> = arrayListOf()

    init {
        colorsList = otherSKUsByGroupKey.keys.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.product_color_selector_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return colorsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colorsList[position])
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(color: String?) {
            itemView.setOnClickListener {
                selectedColor = color
                listener.onColorSelection(selectedColor)
                notifyDataSetChanged()
            }

            color?.let {
                setSelectedColorIcon(itemView.color, otherSKUsByGroupKey[color]?.get(0)?.externalColourRef)
                itemView.border.apply {
                    setBackgroundResource(if (it.equals(selectedColor, true)) R.drawable.product_color_selected_background else R.drawable.product_color_un_selected_background)
                }
            }
        }
    }

    fun setSelect(selectedColor: String?) {
        this.selectedColor = selectedColor
        notifyDataSetChanged()
    }
}

private fun setSelectedColorIcon(mImSelectedColor: WrapContentDraweeView, imageUrl: String?) {
    val drawImage = DrawImage(WoolworthsApplication.getAppContext())
    mImSelectedColor.imageAlpha = if (TextUtils.isEmpty(imageUrl)) 0 else 255
    drawImage.displayImage(mImSelectedColor, imageUrl)
}
