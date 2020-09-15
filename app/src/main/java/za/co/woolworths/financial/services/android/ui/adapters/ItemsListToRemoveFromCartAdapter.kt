package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cart_product_item.view.*
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class ItemsListToRemoveFromCartAdapter(var commerceItems: ArrayList<CommerceItem>) : RecyclerView.Adapter<ItemsListToRemoveFromCartAdapter.ViewHolder>() {


    override fun getItemCount(): Int {
        return commerceItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commerceItems[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cart_product_item, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(commerceItem: CommerceItem) {

            itemView.swipe.isSwipeEnabled = false
            itemView.tvTitle.text = commerceItem.commerceItemInfo.productDisplayName ?: ""
            Utils.truncateMaxLine(itemView.tvTitle)
            itemView.llQuantity.visibility = View.INVISIBLE
            itemView.price.text = WFormatter.formatAmount(commerceItem.priceInfo.amount)
            itemView.rlDeleteButton.visibility = View.GONE
            setProductImage(itemView.cartProductImage, commerceItem.commerceItemInfo.externalImageURL
                    ?: "")
            if (commerceItem.priceInfo.discountedAmount > 0) {
                itemView.promotionalText.text = " " + WFormatter.formatAmount(commerceItem.priceInfo.discountedAmount)
                itemView.promotionalTextLayout.visibility = View.VISIBLE
            } else {
                itemView.promotionalTextLayout.visibility = View.GONE
            }

            if (commerceItem.commerceItemClassType == "foodCommerceItem") {
                itemView.tvSize.visibility = View.INVISIBLE
            } else {
                var sizeAndColor = commerceItem.commerceItemInfo?.color ?: ""
                commerceItem.commerceItemInfo?.apply {
                    if (sizeAndColor.isEmpty() && size.isNotEmpty() && !size.equals("NO SZ", true))
                        sizeAndColor = size
                    else if (sizeAndColor.isNotEmpty() && size.isNotEmpty() && !size.equals("NO SZ", true)) {
                        sizeAndColor = "$sizeAndColor, $size"
                    }
                    itemView.tvSize.text = sizeAndColor
                    itemView.tvSize.visibility = View.VISIBLE
                }
            }

        }

        private fun setProductImage(image: WrapContentDraweeView, imgUrl: String) {
            val url = KotlinUtils.productImageUrlPrefix + imgUrl
            //TODO:: get domain name dynamically
            image.setImageURI(if (TextUtils.isEmpty(imgUrl)) KotlinUtils.productImageUrlPrefix else url)
        }
    }

}