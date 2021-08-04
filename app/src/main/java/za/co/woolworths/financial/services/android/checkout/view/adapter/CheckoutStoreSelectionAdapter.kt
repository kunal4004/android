package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.models.dto.ValidateStoreList
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 13/07/21.
 */
class CheckoutStoreSelectionAdapter(
    private val storeList: List<ValidateStoreList>, private val fragment: Fragment
) : RecyclerView.Adapter<CheckoutStoreSelectionAdapter.StoreViewHolder>(),
    Filterable {

    var checkedItemPosition = -1
    var checkedItemStoreId = "-1"
    var storeFilterList: List<ValidateStoreList>

    init {
        storeFilterList = storeList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        return StoreViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.checkout_store_selection_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return storeFilterList.size
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bindItem(position)
    }

    inner class StoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                storeFilterList[position].let {
                    title.text = it.storeName
                    subTitle.text = it.storeAddress
                    selector.isChecked = checkedItemPosition == position

                    addressSelectionLayout.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    title.paintFlags =
                        if (!it.deliverable!!) Paint.STRIKE_THRU_TEXT_FLAG else Paint.ANTI_ALIAS_FLAG
                }
                setOnClickListener {
                    if (!storeFilterList[position].deliverable!!) return@setOnClickListener

                    val bundle = Bundle()
                    bundle.putString(CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY, Utils.toJson(storeFilterList[position]))
                    fragment.setFragmentResult(CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY, bundle)

                    checkedItemPosition = position
                    checkedItemStoreId = storeFilterList[position].storeId!!
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                storeFilterList = if (charSearch.isEmpty()) {
                    storeList
                } else {
                    storeList.filter {
                        it.storeName?.contains(
                            charSearch,
                            true
                        ) == true
                    } as ArrayList<ValidateStoreList>
                }
                val filterResults = FilterResults()
                filterResults.values = storeFilterList
                var localItemPosition = -1
                (filterResults.values as? List<ValidateStoreList>)?.forEachIndexed { index, it ->
                    if (it.storeId.equals(checkedItemStoreId)) {
                        localItemPosition = index  // To manage checkbox while search.
                    }
                }
                checkedItemPosition = localItemPosition
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                storeFilterList = results?.values as ArrayList<ValidateStoreList>
                notifyDataSetChanged()
            }
        }
    }
}