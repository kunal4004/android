package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import kotlinx.android.synthetic.main.checkout_address_confirmation_selection_delivery_list.view.*

/**
 * Created by Kunal Uttarwar on 11/08/21.
 */
class CheckoutDeliveryTypeSelectionShimmerAdapter(
    private var shouldShowShimmerList: ArrayList<Boolean>
) :
    RecyclerView.Adapter<CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder>() {
    val shimmer: Shimmer = Shimmer.AlphaHighlightBuilder().build()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder {
        return CheckoutDeliveryTypeSelectionViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.checkout_address_confirmation_selection_delivery_list,
                    parent,
                    false
                )
        )
    }

    override fun getItemCount(): Int {
        return shouldShowShimmerList.size
    }

    override fun onBindViewHolder(
        holder: CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder,
        position: Int
    ) {
        holder.bindItem(position)
    }

    inner class CheckoutDeliveryTypeSelectionViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        fun bindItem(position: Int) {
            itemView.apply {
                if (shouldShowShimmerList[position])
                    showShimmer(this)
            }
        }
    }

    private fun showShimmer(view: View) {
        view.apply {
            editAddressImageView.visibility = View.GONE
            selectorShimmerFrameLayout?.setShimmer(shimmer)
            selectorShimmerFrameLayout?.startShimmer()
            selector.visibility = View.INVISIBLE

            titleShimmerLayout?.setShimmer(shimmer)
            titleShimmerLayout?.startShimmer()
            title.visibility = View.INVISIBLE

            subtitleShimmerLayout?.setShimmer(shimmer)
            subtitleShimmerLayout?.startShimmer()
            subTitle.visibility = View.INVISIBLE

            slotPriceButtonShimmerFrameLayout?.setShimmer(shimmer)
            slotPriceButtonShimmerFrameLayout?.startShimmer()
            slotPriceButton.visibility = View.INVISIBLE
        }
    }
}
