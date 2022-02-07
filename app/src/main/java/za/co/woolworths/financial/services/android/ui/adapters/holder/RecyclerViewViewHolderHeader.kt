package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chanel_logo_view.view.*
import kotlinx.android.synthetic.main.item_found_layout.view.*
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.models.dto.ProductList

class RecyclerViewViewHolderHeader(parent: ViewGroup) : RecyclerViewViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_found_layout, parent, false)
) {
    fun setNumberOfItems(activity: FragmentActivity?, productList: ProductList?) {

        /*todo  need to add condition here to show chanel banner*/

//        if (ChanelUtils.isCategoryPresentInConfig(productList?.brandText)) {
//            itemView.view_plp_seperator.visibility = View.VISIBLE
//            itemView.chanel_logo_header?.visibility = View.VISIBLE
//        } else {
//            itemView.view_plp_seperator.visibility = View.GONE
//            itemView.chanel_logo_header?.visibility = View.GONE
//        }

        when (productList?.numberOfItems) {
            1 -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems; itemView.tvFoundItem.text =
                activity?.getString(R.string.product_item)
            }
            else -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems
            }
        }
    }
}