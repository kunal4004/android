package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 17/06/21.
 */
class CheckoutAddressConfirmationListAdapter(
    private var savedAddress: SavedAddressResponse?,
    private val navController: NavController?,
    private val listner: EventListner
) :
    RecyclerView.Adapter<CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder>() {

    var checkedItemPosition = -1

    companion object {
        const val EDIT_SAVED_ADDRESS_RESPONSE_KEY = "editSavedAddressResponse"
        const val EDIT_ADDRESS_POSITION_KEY = "position"
    }

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
        return savedAddress?.addresses?.size ?: 0
    }

    override fun onBindViewHolder(
        holder: CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    fun setData(savedAddressResponse: SavedAddressResponse?) {
        savedAddress = savedAddressResponse
    }

    inner class CheckoutAddressConfirmationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                hideShimmer(this)
                savedAddress?.addresses?.get(position)?.let {
                    // If there is a default address nickname present set it selected
                    if (savedAddress?.defaultAddressNickname == it.nickname) {
                        checkedItemPosition = position
                    }
                    title.text = it.nickname
                    subTitle.text = it.address1
                    selector.isChecked = checkedItemPosition == position
                    addressSelectionLayout.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    title.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    subTitle.setBackgroundColor(
                        if (selector.isChecked) bindColor(R.color.selected_address_background_color) else bindColor(
                            R.color.white
                        )
                    )
                    if (selector.isChecked) {
                        listner.hideErrorView()
                    }
                }

                setOnClickListener {
                    onItemClick(adapterPosition)
                }
                editAddressImageView.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString(EDIT_SAVED_ADDRESS_RESPONSE_KEY, Utils.toJson(savedAddress))
                    bundle.putInt(EDIT_ADDRESS_POSITION_KEY, position)
                    navController?.navigate(
                        R.id.CheckoutAddAddressNewUserFragment,
                        bundleOf("bundle" to bundle)
                    )
                }
            }

        }
    }

    private fun hideShimmer(view: View) {
        view.apply {
            selectorShimmerFrameLayout?.setShimmer(null)
            selectorShimmerFrameLayout?.stopShimmer()
            selector.visibility = View.VISIBLE

            titleShimmerLayout?.setShimmer(null)
            titleShimmerLayout?.stopShimmer()
            title.visibility = View.VISIBLE

            subtitleShimmerLayout?.setShimmer(null)
            subtitleShimmerLayout?.stopShimmer()
            subTitle.visibility = View.VISIBLE

            slotPriceButtonShimmerFrameLayout?.setShimmer(null)
            slotPriceButtonShimmerFrameLayout?.stopShimmer()
            slotPriceButton.visibility = View.GONE
        }
    }

    fun onItemClick(position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        savedAddress?.addresses?.get(position)?.let {
            listner.changeAddress(it)
        }

        checkedItemPosition = position
        notifyDataSetChanged()
    }

    interface EventListner {
        fun hideErrorView()
        fun changeAddress(address: Address)
    }
}
