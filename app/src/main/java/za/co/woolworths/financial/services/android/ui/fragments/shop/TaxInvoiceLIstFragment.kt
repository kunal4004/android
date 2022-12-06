package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentTaxInvoiceListBinding
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.OrderTaxInvoiceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity.Companion.FILE_NAME
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity.Companion.FILE_VALUE
import za.co.woolworths.financial.services.android.ui.activities.WPdfViewerActivity.Companion.PAGE_TITLE
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.TaxInvoiceAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import java.io.UnsupportedEncodingException

class TaxInvoiceLIstFragment : Fragment(R.layout.fragment_tax_invoice_list), TaxInvoiceAdapter.OnItemClick {

    private lateinit var binding: FragmentTaxInvoiceListBinding
    var taxNoteNumbers: ArrayList<String>? = null
    var orderId: String? = null

    companion object {
        private val ARG_PARAM = "taxNoteNumbers"
        private val ORDER_ID = "orderId"

        fun getInstance(orderId: String, taxNoteNumbers: ArrayList<String>) = TaxInvoiceLIstFragment().withArgs {
            putSerializable(ARG_PARAM, taxNoteNumbers)
            putString(ORDER_ID, orderId)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as? BottomNavigationActivity)?.apply {
            hideBottomNavigationMenu()
        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as? BottomNavigationActivity)?.apply {
            showBottomNavigationMenu()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            taxNoteNumbers = getSerializable(ARG_PARAM) as ArrayList<String>
            orderId = getString(ORDER_ID)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaxInvoiceListBinding.bind(view)
        initView()
    }

    private fun initView() {
        binding.apply {
            val orderText = getString(R.string.order_page_title_prefix).plus(orderId)
            btnBack.setOnClickListener {
                activity?.onBackPressed()
            }
            toolbarText.setText(orderText)
            taxInvoiceList.layoutManager =
                LinearLayoutManager(activity) as RecyclerView.LayoutManager?
            taxInvoiceList.adapter = TaxInvoiceAdapter(taxNoteNumbers, this@TaxInvoiceLIstFragment)
        }
    }

    override fun onItemSelection(taxNumber: String) {
        loadTaxInvoice(taxNumber)
    }

    private fun loadTaxInvoice(taxNumber: String) {
        showProgressBar()

        val getInvoiceOrderRequest = OneAppService.getOrderTaxInvoice(taxNumber)
        getInvoiceOrderRequest.enqueue(CompletionHandler(object : IResponseListener<OrderTaxInvoiceResponse> {
            override fun onSuccess(orderTaxInvoiceResponse: OrderTaxInvoiceResponse?) {
                hideProgressBar()
                orderTaxInvoiceResponse?.data?.let { response -> buildTaxInvoicePDF(response) }
            }

            override fun onFailure(error: Throwable?) {
                hideProgressBar()
            }

        }, OrderTaxInvoiceResponse::class.java))

    }

    fun buildTaxInvoicePDF(data: String) {
        try {
            val byteArray = Base64.decode(data, Base64.DEFAULT)
            showTAxInvoice(byteArray)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
    }

    private fun showTAxInvoice(fileData: ByteArray) {

        activity?.apply {
            Intent(activity, WPdfViewerActivity::class.java).apply {
                putExtra(FILE_NAME, orderId)
                putExtra(FILE_VALUE, fileData)
                putExtra(PAGE_TITLE, getString(R.string.order_invoice_title_prefix) + orderId)
                startActivity(this)
            }
        }

    }


    private fun hideProgressBar() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity?.runOnUiThread {
            binding.loadingBar.visibility = View.INVISIBLE
        }
    }

    private fun showProgressBar() {
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity?.runOnUiThread {
            binding.loadingBar.visibility = View.VISIBLE
        }
    }


}
