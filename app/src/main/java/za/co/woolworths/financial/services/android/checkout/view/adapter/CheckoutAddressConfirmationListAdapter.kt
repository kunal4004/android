package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.ui.extension.bindColor

/**
 * Created by Kunal Uttarwar on 17/06/21.
 */
class CheckoutAddressConfirmationListAdapter(private var savedAddress: SavedAddressResponse?) :
    RecyclerView.Adapter<CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder>() {

    var checkedItemPosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder {
        return CheckoutAddressConfirmationViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.checkout_address_confirmation_selection_delivery_list,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        if (savedAddress?.addresses.isNullOrEmpty())
            return 0
        return savedAddress?.addresses?.size!!
    }

    override fun onBindViewHolder(
        holder: CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    inner class CheckoutAddressConfirmationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                savedAddress?.addresses?.get(position)?.let {
                    title.text = it.displayName
                    subTitle.text = it.address1
                    selector.isChecked = checkedItemPosition == position
                    addressSelectionLayout.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                }

                setOnClickListener {
                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
                editAddressImageView.setOnClickListener {

                }
            }

        }
    }
}
