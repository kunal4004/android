package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutStoreSelectionListBinding
import za.co.woolworths.financial.services.android.checkout.view.CheckoutAddressConfirmationFragment
import za.co.woolworths.financial.services.android.models.dto.ValidateStoreList
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 13/07/21.
 */
class CheckoutStoreSelectionAdapter(
    private val storeList: List<ValidateStoreList>,
    private val fragment: Fragment,
    private val itemPosition: Int
) : RecyclerView.Adapter<CheckoutStoreSelectionAdapter.StoreViewHolder>(),
    Filterable {

    var checkedItemPosition: Int
    var checkedItemStoreId = "-1"
    var storeFilterList: List<ValidateStoreList>

    init {
        storeFilterList = storeList
        checkedItemPosition = itemPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        return StoreViewHolder(
            CheckoutStoreSelectionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        )
    }

    override fun getItemCount(): Int {
        return storeFilterList.size
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bindItem(position)
    }

    inner class StoreViewHolder(private val binding: CheckoutStoreSelectionListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(position: Int) {
            binding.apply {
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
                    if (selector.isChecked && storeFilterList[position].deliverable == true) {
                        val bundle = Bundle()
                        bundle.putString(
                            CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY,
                            Utils.toJson(storeFilterList[position])
                        )
                        fragment.setFragmentResult(
                            CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY,
                            bundle
                        )
                    }
                }

                this.root.setOnClickListener {
                    if (storeFilterList[position].deliverable == false) return@setOnClickListener

                    val bundle = Bundle()
                    bundle.putString(
                        CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY,
                        Utils.toJson(storeFilterList[position])
                    )
                    fragment.setFragmentResult(
                        CheckoutAddressConfirmationFragment.STORE_SELECTION_REQUEST_KEY,
                        bundle
                    )

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
                (filterResults.values as? List<ValidateStoreList>)?.forEachIndexed { index, it ->
                    if (it.storeId.equals(checkedItemStoreId)) {
                        checkedItemPosition = index  // To manage checkbox while search.
                    }
                }
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