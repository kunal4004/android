package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiOverviewRowBinding
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindDimens
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

internal class BalanceProtectionInsuranceAdapter(private val bpiOverviewList: MutableList<BalanceProtectionInsuranceOverview>?, val onClickListener: (BalanceProtectionInsuranceOverview) -> Unit)
    : RecyclerView.Adapter<BalanceProtectionInsuranceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BpiOverviewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bpiOverview: BalanceProtectionInsuranceOverview? = bpiOverviewList?.get(position)
        val insuranceType: InsuranceType? = bpiOverview?.insuranceType
        holder.bindItems(bpiOverview)
        insuranceType?.let { holder.verticalBarBackground(it) }
        insuranceType?.let { holder.itemIsCovered(it) }
    }

    override fun getItemCount(): Int = bpiOverviewList?.size ?: 0

   inner class ViewHolder(val binding: BpiOverviewRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(bpiOverview: BalanceProtectionInsuranceOverview?) {
            with(binding) {
                setOverviewConstraint(
                    itemCount,
                    clBalanceOverview,
                    adapterPosition,
                    R.dimen.seventeen_dp,
                    R.dimen.sixteen_dp
                )

                val header = bpiOverview?.overview?.header ?: bpiOverview?.overview?.title
                tvTitle?.text = header?.let { bindString(it) } ?: ""
                tvDescription?.text =
                    bpiOverview?.overview?.description?.let { bindString(it) }
                bpiOverview?.overviewDrawable?.let {
                    imOverViewDescImage.setImageResource(
                        it
                    )
                }

                AnimationUtilExtension.animateViewPushDown(itemView)

                itemView.setOnClickListener {
                    bpiOverview?.let { onClickListener(it) }
                }
            }
        }

        fun verticalBarBackground(insuranceType: InsuranceType) {
            binding.spVerticalOrangeDeco?.setBackgroundColor(if (insuranceType.covered) bindColor(R.color.bpi_orange) else bindColor(R.color.black))
        }

        fun itemIsCovered(insuranceType: InsuranceType) {
            if (insuranceType.covered) {
                binding.tvCover?.visibility = View.VISIBLE
                binding.imRightArrow?.visibility = View.GONE
            } else {
                binding.tvCover?.visibility = View.GONE
                binding.imRightArrow?.visibility = View.VISIBLE
            }
        }

        // Set top and bottom margin for bpi overview adapter row
        private fun setOverviewConstraint(itemCount: Int, view: View, position: Int, topMargin: Int?, bottomMargin: Int?) {
            val marginTop = topMargin?.let { bindDimens(it) }
            val marginBottom = bottomMargin?.let { bindDimens(it) }
            val params = view.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(params.leftMargin,
                (if (position == 0) marginTop else params.topMargin) as? Int ?: 0, params.rightMargin,
                (if (position == (itemCount - 1)) marginBottom else params.bottomMargin) as? Int ?: 0
            )
            view.layoutParams = params
        }
    }
}