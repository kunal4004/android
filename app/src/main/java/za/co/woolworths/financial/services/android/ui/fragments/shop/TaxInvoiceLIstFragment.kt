package za.co.woolworths.financial.services.android.ui.fragments.shop


import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_tax_invoice_list.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.dto.OrderTaxInvoiceResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.OrdersTaxInvoiceActivity
import za.co.woolworths.financial.services.android.ui.adapters.TaxInvoiceAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.PermissionResultCallback
import za.co.woolworths.financial.services.android.util.PermissionUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class TaxInvoiceLIstFragment : Fragment(), TaxInvoiceAdapter.OnItemClick, PermissionResultCallback {

    var taxNoteNumbers: ArrayList<String>? = null
    var orderId: String? = null
    private var permissionUtils: PermissionUtils? = null
    var permissions: java.util.ArrayList<String> = arrayListOf()
    var selectedInvoiceNumber: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tax_invoice_list, container, false)
    }

    companion object {
        private val ARG_PARAM = "taxNoteNumbers"
        private val ORDER_ID = "orderId"

        fun getInstance(orderId: String, taxNoteNumbers: ArrayList<String>) = TaxInvoiceLIstFragment().withArgs {
            putSerializable(ARG_PARAM, taxNoteNumbers)
            putString(ORDER_ID, orderId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            taxNoteNumbers = arguments.getSerializable(ARG_PARAM) as ArrayList<String>
            orderId = arguments.getString(ORDER_ID)
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        permissionUtils = PermissionUtils(activity, this)
        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        initView()
    }

    private fun initView() {
        taxInvoiceList.layoutManager = LinearLayoutManager(activity) as RecyclerView.LayoutManager?
        taxInvoiceList.adapter = TaxInvoiceAdapter(taxNoteNumbers, this)
    }

    override fun onItemSelection(taxNumber: String) {
        selectedInvoiceNumber = taxNumber
        checkExternalStoragePermission()
    }

    private fun checkExternalStoragePermission() {
        permissionUtils?.check_permission(permissions, "Explain here why the app needs permissions", 1)
    }

    override fun PermissionGranted(request_code: Int) {
        loadTaxInvoice()
    }

    override fun PartialPermissionGranted(request_code: Int, granted_permissions: java.util.ArrayList<String>?) {
    }

    override fun PermissionDenied(request_code: Int) {
    }

    override fun NeverAskAgain(request_code: Int) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun loadTaxInvoice() {
        showProgressBar()
        var file = File(Environment.getExternalStorageDirectory().getPath(), "Woolworths/TaxInvoice/" + orderId + "_" + selectedInvoiceNumber!! + ".pdf")
        if (file.exists()) {
            hideProgressBar()
            showTAxInvoice(file.path)
        } else {
            selectedInvoiceNumber?.let { selectedInvoiceNumber ->
               val  getInvoiceOrderRequest = OneAppService.getOrderTaxInvoice(selectedInvoiceNumber)
                getInvoiceOrderRequest.enqueue(CompletionHandler(object: RequestListener<OrderTaxInvoiceResponse> {
                    override fun onSuccess(orderTaxInvoiceResponse: OrderTaxInvoiceResponse?) {
                        hideProgressBar()
                        orderTaxInvoiceResponse?.data?.let { response -> buildTaxInvoicePDF(response)  }
                    }

                    override fun onFailure(error: Throwable?) {
                        hideProgressBar()
                    }

                }))
            }
        }
    }

    fun buildTaxInvoicePDF(data: String) {
        var filePathAndStatus: GetFilePathAndStatus = getFileFromBase64AndSaveInSDCard(data, orderId + "_" + selectedInvoiceNumber!!, "pdf")
        if (filePathAndStatus.filStatus && File(filePathAndStatus.filePath).exists()) showTAxInvoice(filePathAndStatus.filePath!!) else return
    }

    private fun showTAxInvoice(filePath: String) {
        var intent: Intent = Intent(activity, OrdersTaxInvoiceActivity::class.java)
        intent.putExtra("filePath", filePath)
        intent.putExtra("orderId", orderId)
        activity.startActivity(intent)
    }

    fun getFileFromBase64AndSaveInSDCard(base64: String, filename: String, extension: String): GetFilePathAndStatus {
        val getFilePathAndStatus = GetFilePathAndStatus()
        try {
            val pdfAsBytes = Base64.decode(base64, 0)
            val os: FileOutputStream
            os = FileOutputStream(getReportPath(filename, extension), false)
            os.write(pdfAsBytes)
            os.flush()
            os.close()
            getFilePathAndStatus.filStatus = true
            getFilePathAndStatus.filePath = getReportPath(filename, extension)
            return getFilePathAndStatus
        } catch (e: IOException) {
            e.printStackTrace()
            getFilePathAndStatus.filStatus = false
            getFilePathAndStatus.filePath = getReportPath(filename, extension)
            return getFilePathAndStatus
        }

    }

    fun getReportPath(filename: String, extension: String): String {
        val file = File(Environment.getExternalStorageDirectory().getPath(), "Woolworths/TaxInvoice")
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.getAbsolutePath() + "/" + filename + "." + extension

    }

    class GetFilePathAndStatus {
        var filStatus: Boolean = false
        var filePath: String? = null

    }

    private fun hideProgressBar() {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity.runOnUiThread {
            loadingBar.visibility = View.INVISIBLE
        }
    }

    private fun showProgressBar() {
        activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        activity.runOnUiThread {
            loadingBar.visibility = View.VISIBLE
        }
    }


}
