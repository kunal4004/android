package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.overview

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_overview_row.view.*
import za.co.woolworths.financial.services.android.models.dto.BalanceProtectionInsuranceOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindDimens
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

internal class BalanceProtectionInsuranceAdapter(private val bpiOverviewList: MutableList<BalanceProtectionInsuranceOverview>?, val onClickListener: (BalanceProtectionInsuranceOverview) -> Unit)
    : RecyclerView.Adapter<BalanceProtectionInsuranceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.bpi_overview_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bpiOverview: BalanceProtectionInsuranceOverview? = bpiOverviewList?.get(position)
        val insuranceType: InsuranceType? = bpiOverview?.insuranceType
        holder.bindItems(bpiOverview)
        insuranceType?.let { holder.verticalBarBackground(it) }
        insuranceType?.let { holder.itemIsCovered(it) }
    }

    override fun getItemCount(): Int = bpiOverviewList?.size ?: 0

   inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(bpiOverview: BalanceProtectionInsuranceOverview?) {
            setOverviewConstraint(
                itemCount,
                itemView.clBalanceOverview,
                adapterPosition,
                R.dimen.seventeen_dp,
                R.dimen.sixteen_dp
            )

             val header = bpiOverview?.overview?.header ?: bpiOverview?.overview?.title
            itemView.tvTitle?.text = header?.let { bindString(it) } ?: ""
            itemView.tvDescription?.text = bpiOverview?.overview?.description?.let { bindString(it) }
            bpiOverview?.overviewDrawable?.let { itemView.imOverViewDescImage.setImageResource(it) }

            AnimationUtilExtension.animateViewPushDown(itemView)

            itemView.setOnClickListener {
                bpiOverview?.let { onClickListener(it) }
            }
        }

        fun verticalBarBackground(insuranceType: InsuranceType) {
            itemView.spVerticalOrangeDeco?.setBackgroundColor(if (insuranceType.covered) bindColor(R.color.bpi_orange) else bindColor(R.color.black))
        }

        fun itemIsCovered(insuranceType: InsuranceType) {
            if (insuranceType.covered) {
                itemView.tvCover?.visibility = View.VISIBLE
                itemView.imRightArrow?.visibility = View.GONE
            } else {
                itemView.tvCover?.visibility = View.GONE
                itemView.imRightArrow?.visibility = View.VISIBLE
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