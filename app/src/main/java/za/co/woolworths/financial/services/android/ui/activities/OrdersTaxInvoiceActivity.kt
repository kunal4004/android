package za.co.woolworths.financial.services.android.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_oreder_tax_invoice.*
import za.co.woolworths.financial.services.android.util.Utils
import java.io.File
import android.content.Intent
import android.net.Uri


class OrdersTaxInvoiceActivity : AppCompatActivity() {

    var filePath: String? = null
    var orderID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oreder_tax_invoice)
        Utils.updateStatusBarBackground(this)
        filePath = intent.getStringExtra("filePath")
        orderID = intent.getStringExtra("orderId")
        initView()
    }

    private fun initView() {
        done.setOnClickListener { onBackPressed() }
        share.setOnClickListener { shareInvoice() }
        configureUI()
    }

    private fun configureUI() {
        toolbarText.text = getString(R.string.order_invoice_title_prefix) + orderID
        val file = File(filePath)
        pdfView.fromFile(file)
                .enableDoubletap(true)
                .defaultPage(0)
                .scrollHandle(null)
                .enableAnnotationRendering(false)
                .enableAntialiasing(false)
                .spacing(0)
                .load()
    }

    private fun shareInvoice() {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(filePath)

        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "application/pdf"
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"))
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
