package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_tax_invoice_list.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import kotlinx.android.synthetic.main.fragment_tax_invoice_list.view.*
import za.co.woolworths.financial.services.android.ui.adapters.TaxInvoiceAdapter

class TaxInvoiceLIstFragment : Fragment() {

    var taxNoteNumbers: ArrayList<String>? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tax_invoice_list, container, false)
    }

    companion object {
        private val ARG_PARAM = "taxNoteNumbers"

        fun getInstance(taxNoteNumbers: ArrayList<String>) = TaxInvoiceLIstFragment().withArgs {
            putSerializable(ARG_PARAM, taxNoteNumbers)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            taxNoteNumbers = arguments.getSerializable(ARG_PARAM) as ArrayList<String>
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        taxInvoiceList.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        taxInvoiceList.adapter = TaxInvoiceAdapter(taxNoteNumbers)
    }


}
