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
            /*
       tvColorSize = view.findViewById(R.id.tvSize);
       */

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
                itemView.promotionalText.visibility = View.VISIBLE
            } else {
                itemView.promotionalText.visibility = View.GONE
            }

            /*
            // Set Color and Size START
            if (itemRow.category.equalsIgnoreCase("FOOD")) {
                producttvColorSize.setVisibility(View.INVISIBLE);
            } else {
                String sizeColor = (commerceItemInfo == null) ? "" : commerceItemInfo.getColor();
                if (sizeColor == null)
                    sizeColor = "";
                if (commerceItemInfo != null) {
                    if (sizeColor.isEmpty() && !commerceItemInfo.getSize().isEmpty() && !commerceItemInfo.getSize().equalsIgnoreCase("NO SZ"))
                        sizeColor = commerceItemInfo.getSize();
                    else if (!sizeColor.isEmpty() && !commerceItemInfo.getSize().isEmpty() && !commerceItemInfo.getSize().equalsIgnoreCase("NO SZ"))
                        sizeColor = sizeColor + ", " + commerceItemInfo.getSize();
                }
                producttvColorSize.setText(sizeColor);
                producttvColorSize.setVisibility(View.VISIBLE);
            }*/
        }

        private fun setProductImage(image: WrapContentDraweeView, imgUrl: String) {
            val url = "https://images.woolworthsstatic.co.za/" + imgUrl;
            //TODO:: get domain name dynamically
            image.setImageURI(if (TextUtils.isEmpty(imgUrl)) "https://images.woolworthsstatic.co.za/" else url);
        }
    }

}