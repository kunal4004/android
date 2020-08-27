package za.co.woolworths.financial.services.android.ui.adapters

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.cart_product_item.view.*
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.ui.views.WrapContentDraweeView
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class UnsellableItemsListAdapter(var commerceItems: ArrayList<UnSellableCommerceItem>) : RecyclerView.Adapter<UnsellableItemsListAdapter.ViewHolder>() {


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
        fun bind(commerceItem: UnSellableCommerceItem) {

            itemView.swipe.isSwipeEnabled = false
            itemView.tvTitle.text = commerceItem.productDisplayName ?: ""
            Utils.truncateMaxLine(itemView.tvTitle)
            itemView.llQuantity.visibility = View.INVISIBLE
            itemView.price.text = commerceItem.price.amount.let { WFormatter.formatAmount(it) }
            itemView.rlDeleteButton.visibility = View.GONE
            setProductImage(itemView.cartProductImage, commerceItem.externalImageURL
                    ?: "")
            if (commerceItem.price.getDiscountedAmount() > 0) {
                itemView.promotionalText.text = " " + WFormatter.formatAmount(commerceItem.price.getDiscountedAmount())
                itemView.promotionalTextLayout.visibility = View.VISIBLE
            } else {
                itemView.promotionalTextLayout.visibility = View.GONE
            }

            if (commerceItem.commerceItemClassType == "foodCommerceItem") {
                itemView.tvSize.visibility = View.INVISIBLE
            } else {
                var sizeAndColor = commerceItem.colour ?: ""
                commerceItem.apply {
                    if (sizeAndColor.isEmpty() && !size.isNullOrEmpty() && !size.equals("NO SZ", true))
                        sizeAndColor = size
                    else if (sizeAndColor.isNotEmpty() && !size.isNullOrEmpty() && !size.equals("NO SZ", true)) {
                        sizeAndColor = "$sizeAndColor, $size"
                    }
                    itemView.tvSize.text = sizeAndColor
                    itemView.tvSize.visibility = View.VISIBLE
                }
            }

        }

        private fun setProductImage(image: WrapContentDraweeView, imgUrl: String) {
            val url = "https://images.woolworthsstatic.co.za/" + imgUrl;
            //TODO:: get domain name dynamically
            image.setImageURI(if (TextUtils.isEmpty(imgUrl)) "https://images.woolworthsstatic.co.za/" else url);
        }
    }

}