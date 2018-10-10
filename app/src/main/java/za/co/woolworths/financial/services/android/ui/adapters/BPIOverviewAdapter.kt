package za.co.woolworths.financial.services.android.ui.adapters

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_overview_row.view.*
import za.co.woolworths.financial.services.android.models.dto.BPIOverview
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.util.setOverviewConstraint

internal class BPIOverviewAdapter(private val bpiOverviewList: MutableList<BPIOverview>?, private val onBPIAdapterClickListener: OnBPIAdapterClickListener)
    : RecyclerView.Adapter<BPIOverviewAdapter.ViewHolder>() {

    companion object {
        var mOnBPIOverviewAdapter: OnBPIAdapterClickListener? = null
    }

    interface OnBPIAdapterClickListener {
        fun onItemViewClicked(bpiOverview: BPIOverview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPIOverviewAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.bpi_overview_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: BPIOverviewAdapter.ViewHolder, position: Int) {
        val bpiOverview: BPIOverview = bpiOverviewList!![position]
        val insuranceType: InsuranceType = bpiOverview.insuranceType!!
        mOnBPIOverviewAdapter = onBPIAdapterClickListener
        holder.bindItems(bpiOverview)
        holder.verticalBarBackground(insuranceType)
        holder.itemIsCovered(insuranceType)
        holder.onItemClick(bpiOverview)
    }

    override fun getItemCount(): Int {
        return bpiOverviewList?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(bpiOverview: BPIOverview) {
            itemView.clBalanceOverview.setOverviewConstraint(adapterPosition, R.dimen.seventeen_dp, R.dimen.sixteen_dp)
            itemView.tvTitle.text = bpiOverview.overviewTitle
            itemView.tvDescription.text = bpiOverview.overviewDescription
            itemView.imOverViewDescImage.setImageResource(bpiOverview.overviewDrawable!!)
        }

        fun verticalBarBackground(insuranceType: InsuranceType) {
            itemView.spVerticalOrangeDeco.setBackgroundColor(if (insuranceType.covered) ContextCompat.getColor(itemView.context, R.color.bpi_orange)
            else ContextCompat.getColor(itemView.context, R.color.black))
        }

        fun itemIsCovered(insuranceType: InsuranceType) {
            if (insuranceType.covered) {
                itemView.tvCover.visibility = View.VISIBLE
                itemView.imRightArrow.visibility = View.GONE
            } else {
                itemView.tvCover.visibility = View.GONE
                itemView.imRightArrow.visibility = View.VISIBLE
            }
        }

        fun onItemClick(bpiOverview: BPIOverview) {
            itemView.setOnClickListener {
                if (mOnBPIOverviewAdapter != null)
                    mOnBPIOverviewAdapter?.onItemViewClicked(bpiOverview)
            }
        }
    }
}