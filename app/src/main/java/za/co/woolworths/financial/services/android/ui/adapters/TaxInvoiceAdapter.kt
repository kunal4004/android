package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.TaxInvoiceListItemBinding

class TaxInvoiceAdapter(var taxNoteNumbers: ArrayList<String>?, val listner: OnItemClick) : RecyclerView.Adapter<TaxInvoiceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TaxInvoiceListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taxNumber = taxNoteNumbers?.get(position)
        holder.bindText(taxNumber!!, listner)
    }

    override fun getItemCount(): Int {
        return taxNoteNumbers?.size ?: 0
    }

    class ViewHolder(val itemBinding: TaxInvoiceListItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bindText(taxNumber: String, listner: OnItemClick) {
            itemBinding.taxNumber.text = taxNumber
            itemBinding.root.setOnClickListener { listner.onItemSelection(taxNumber) }
        }

    }

    interface OnItemClick {
        fun onItemSelection(taxNumber: String)
    }

}