package za.co.woolworths.financial.services.android.checkout.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.CheckoutAddressConfirmationSelectionDeliveryListBinding
import com.facebook.shimmer.Shimmer

/**
 * Created by Kunal Uttarwar on 11/08/21.
 */
class CheckoutDeliveryTypeSelectionShimmerAdapter(private var shimmerListCount: Int) :
    RecyclerView.Adapter<CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder>() {
    val shimmer: Shimmer = Shimmer.AlphaHighlightBuilder().build()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder {
        return CheckoutDeliveryTypeSelectionViewHolder(
            CheckoutAddressConfirmationSelectionDeliveryListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return shimmerListCount
    }

    override fun onBindViewHolder(
        holder: CheckoutDeliveryTypeSelectionShimmerAdapter.CheckoutDeliveryTypeSelectionViewHolder,
        position: Int
    ) {
        holder.bindItem()
    }

    inner class CheckoutDeliveryTypeSelectionViewHolder(private val binding: CheckoutAddressConfirmationSelectionDeliveryListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItem() {
            itemView.apply {
                binding.showShimmer()
            }
        }
    }

    private fun CheckoutAddressConfirmationSelectionDeliveryListBinding.showShimmer() {
            editAddressImageView.visibility = View.GONE
            selectorShimmerFrameLayout.setShimmer(shimmer)
            selectorShimmerFrameLayout.startShimmer()
            selector.visibility = View.INVISIBLE

            titleShimmerLayout.setShimmer(shimmer)
            titleShimmerLayout.startShimmer()
            title.visibility = View.INVISIBLE

            subtitleShimmerLayout.setShimmer(shimmer)
            subtitleShimmerLayout.startShimmer()
            subTitle.visibility = View.INVISIBLE

            slotPriceButtonShimmerFrameLayout.setShimmer(shimmer)
            slotPriceButtonShimmerFrameLayout.startShimmer()
            slotPriceButton.visibility = View.INVISIBLE
        }
}
