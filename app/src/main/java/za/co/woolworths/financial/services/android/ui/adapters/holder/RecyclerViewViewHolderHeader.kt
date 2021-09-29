package za.co.woolworths.financial.services.android.ui.adapters.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.item_found_layout.view.*
import za.co.woolworths.financial.services.android.models.dto.ProductList

class RecyclerViewViewHolderHeader(parent: ViewGroup) : RecyclerViewViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.item_found_layout, parent, false)
) {
    fun setNumberOfItems(activity: FragmentActivity?, productList: ProductList?) {
        when (productList?.numberOfItems) {
            1, 0 -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems; itemView.tvFoundItem.text =
                activity?.getString(R.string.product_item)
            }
            else -> productList?.numberOfItems?.toString()?.let { numberOfItems ->
                itemView.tvNumberOfItem.text = numberOfItems
            }
        }
    }
}