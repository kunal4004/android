package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.submit_claim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.balance_insurance_item.view.*
import za.co.woolworths.financial.services.android.models.dto.bpi.ClaimReason
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable

internal class BPISubmitClaimAdapter(private val claimReasonList: MutableList<ClaimReason>?, val onClickListener: (Int) -> Unit) :
    RecyclerView.Adapter<BPISubmitClaimAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.balance_insurance_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val claimReason: ClaimReason? = claimReasonList?.get(position)
        holder.bindItems(claimReason)
    }

    override fun getItemCount(): Int = claimReasonList?.size ?: 0

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(claimReason: ClaimReason?) {
            with(itemView) {
                tvTitle?.text = claimReason?.title
                tvDescription?.text = claimReason?.description

                when (adapterPosition) {
                    0 -> {
                        vEmptySpace?.visibility = View.VISIBLE
                        rlBalanceInsurance?.background = bindDrawable(R.drawable.top_divider_list)

                    }
                    itemCount -> itemView.vBottomLine?.visibility = View.VISIBLE

                    else -> {
                        vEmptySpace?.visibility = View.GONE
                        rlBalanceInsurance?.background = bindDrawable(R.drawable.bottom_divider_list)
                    }
                }

                setOnClickListener { onClickListener(adapterPosition) }
            }
        }
    }
}