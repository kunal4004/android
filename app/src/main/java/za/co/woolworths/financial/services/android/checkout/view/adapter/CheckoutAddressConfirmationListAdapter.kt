package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CheckoutAddressConfirmationSelectionDeliveryListBinding
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.util.Utils

/**
 * Created by Kunal Uttarwar on 17/06/21.
 */
class CheckoutAddressConfirmationListAdapter(
    private var savedAddress: SavedAddressResponse?,
    private val navController: NavController?,
    private val listner: EventListner,
    private val activity: FragmentActivity?
) :
    RecyclerView.Adapter<CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder>() {

    var checkedItemPosition = -1

    init {
        // If there is a default address nickname present set it selected
        savedAddress?.addresses?.forEach { address ->
            if (savedAddress?.defaultAddressNickname == address.nickname) {
                checkedItemPosition = savedAddress?.addresses?.indexOf(address) ?: -1
                onItemClick(checkedItemPosition)
                return@forEach
            }
        }
    }

    companion object {
        const val EDIT_SAVED_ADDRESS_RESPONSE_KEY = "editSavedAddressResponse"
        const val EDIT_ADDRESS_POSITION_KEY = "position"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutAddressConfirmationListAdapter.CheckoutAddressConfirmationViewHolder {
        return CheckoutAddressConfirmationViewHolder(
            CheckoutAddressConfirmationSelectionDeliveryListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class CheckoutAddressConfirmationViewHolder(val binding: CheckoutAddressConfirmationSelectionDeliveryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem(position: Int) {
            with(binding) {
                itemView.apply {
                    hideShimmer(this)
                    savedAddress?.addresses?.get(position)?.let {
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
                        Utils.triggerFireBaseEvents(
                            FirebaseManagerAnalyticsProperties.CHANGE_FULFILLMENT_EDIT_ADDRESS,
                            hashMapOf(
                                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_NATIVE_CHECKOUT_EDIT_ADDRESS
                            ),
                            activity
                        )
                        bundle.putString(
                            EDIT_SAVED_ADDRESS_RESPONSE_KEY,
                            Utils.toJson(savedAddress)
                        )
                        bundle.putInt(EDIT_ADDRESS_POSITION_KEY, position)
                        navController?.navigate(
                            R.id.action_checkoutAddressConfirmationFragment_to_CheckoutAddAddressNewUserFragment,
                            bundle
                        )
                    }
                }
            }
        }
    }

    private fun CheckoutAddressConfirmationSelectionDeliveryListBinding.hideShimmer(view: View) {
        view.apply {
            selectorShimmerFrameLayout.setShimmer(null)
            selectorShimmerFrameLayout.stopShimmer()
            selector.visibility = View.VISIBLE

            titleShimmerLayout.setShimmer(null)
            titleShimmerLayout.stopShimmer()
            title.visibility = View.VISIBLE

            subtitleShimmerLayout.setShimmer(null)
            subtitleShimmerLayout.stopShimmer()
            subTitle.visibility = View.VISIBLE

            slotPriceButtonShimmerFrameLayout.setShimmer(null)
            slotPriceButtonShimmerFrameLayout.stopShimmer()
            slotPriceButton.visibility = View.GONE
        }
    }

    fun onItemClick(position: Int) {
        if (position < 0 || position >= itemCount) {
            return
        }
        savedAddress?.addresses?.get(position)?.let {
            listner.changeAddress(it)
            notifyItemChanged(position, it)
        }
        // update last position as well
        val previousPosition = checkedItemPosition
        checkedItemPosition = position

        if (previousPosition < 0 || previousPosition >= itemCount) {
            return
        }
        savedAddress?.addresses?.get(previousPosition)?.let {
            notifyItemChanged(previousPosition, it)
        }

    }

    interface EventListner {
        fun hideErrorView()
        fun changeAddress(address: Address)
    }
}
