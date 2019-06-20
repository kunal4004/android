package za.co.woolworths.financial.services.android.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.absa.openbankingapi.woolworths.integration.dto.ArchivedStatement
import kotlinx.android.synthetic.main.account_e_statement_row.view.*

class AbsaStatementsAdapter(var data: List<ArchivedStatement>) : RecyclerView.Adapter<AbsaStatementsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.account_e_statement_row, parent, false)
        )
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(data[position])

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: ArchivedStatement) {
            itemView.tvStatementName.text = item?.documentWorkingDate
        }
    }
}