package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
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
                    if (selector.isChecked){
                        listner.hideErrorView()
                    }
                }

                setOnClickListener {
                    savedAddress?.addresses?.get(position)?.let {
                        listner.changeAddress(it.nickname)
                    }

                    checkedItemPosition = position
                    notifyDataSetChanged()
                }
                editAddressImageView.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("editSavedAddress", Utils.toJson(savedAddress?.addresses?.get(position)))
                    navController?.navigate(
                        R.id.CheckoutAddAddressNewUserFragment,
                        bundleOf("bundle" to bundle)
                    )
                }
            }

        }
    }

    interface EventListner {
        fun hideErrorView()
        fun changeAddress(nickName: String)
    }
}
