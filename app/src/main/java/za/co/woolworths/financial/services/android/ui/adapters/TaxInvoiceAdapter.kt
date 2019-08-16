package za.co.woolworths.financial.services.android.ui.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.tax_invoice_list_item.view.*

class TaxInvoiceAdapter(var taxNoteNumbers: ArrayList<String>?, val listner: OnItemClick) : RecyclerView.Adapter<TaxInvoiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaxInvoiceAdapter.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.tax_invoice_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: TaxInvoiceAdapter.ViewHolder, position: Int) {
        val taxNumber = taxNoteNumbers?.get(position)
        holder.bindText(taxNumber!!, listner)
    }

    override fun getItemCount(): Int {
        return taxNoteNumbers?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindText(taxNumber: String, listner: OnItemClick) {
            itemView.taxNumber.text = taxNumber
            itemView.setOnClickListener { listner.onItemSelection(taxNumber) }
        }

    }

    interface OnItemClick {
        fun onItemSelection(taxNumber: String)
    }

}