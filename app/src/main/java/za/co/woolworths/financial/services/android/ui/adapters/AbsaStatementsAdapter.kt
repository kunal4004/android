package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.AccountEStatementRowBinding
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import za.co.woolworths.financial.services.android.util.WFormatter

class AbsaStatementsAdapter(var data: List<ArchivedStatement>, var listner: ActionListners) : RecyclerView.Adapter<AbsaStatementsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            AccountEStatementRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
        holder.itemBinding.tvViewStatement.setOnClickListener { listner.onViewStatement(data[position]) }
    }

    class ViewHolder(val itemBinding: AccountEStatementRowBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: ArchivedStatement) {
            itemBinding.tvStatementName.text = WFormatter.formatStatementsDate(item?.documentWorkingDate)
            itemBinding.imCheckItem.visibility = View.GONE
        }
    }

    interface ActionListners {
        fun onViewStatement(item: ArchivedStatement)
    }
}