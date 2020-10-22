package za.co.woolworths.financial.services.android.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_oreder_tax_invoice.*
import za.co.woolworths.financial.services.android.util.Utils
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import com.awfs.coordination.BuildConfig
import com.crashlytics.android.Crashlytics
import za.co.woolworths.financial.services.android.ui.fragments.statement.StatementFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.OneAppEvents
import java.io.FileOutputStream
import java.lang.Exception

class WPdfViewerActivity : AppCompatActivity() {

    var pageTitle: String? = null
    var fileName: String? = null
    var fileData: ByteArray? = null
    var cacheFile: File? = null
    var gtmTag: String? = null
    var navigatedFrom: String? = null

    companion object {
        const val FILE_NAME = "FILE_NAME"
        const val FILE_VALUE = "FILE_VALUE"
        const val PAGE_TITLE = "PAGE_TITLE"
        const val GTM_TAG = "GTM_TAG"
        const val REQUEST_CODE_SHARE = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oreder_tax_invoice)
        getBundleArgument()
        //https://wigroup2.atlassian.net/browse/WOP-6922
        // As of now changing color only for PL statements
        if(StatementFragment.TAG == navigatedFrom){
            Utils.updateStatusBarBackground(this)
            setupToolbar()
        } else {
            Utils.updateStatusBarBackground(this, R.color.black)
        }
        initView()
    }

    private fun setupToolbar() {
        mToolbar.background = ContextCompat.getDrawable(this, R.color.white)
        toolbarText.setTextColor(ContextCompat.getColor(this, R.color.black))
        share.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_share_black))
    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            pageTitle = getString(PAGE_TITLE)
            fileName = getString(FILE_NAME)
            fileData = getByteArray(FILE_VALUE)
            gtmTag = getString(GTM_TAG)
            navigatedFrom = getString(AppConstant.NAVIGATED_FROM)
        }
    }

    private fun initView() {
        done.setOnClickListener { onBackPressed() }
        share.setOnClickListener { shareInvoice() }
        configureUI()
    }

    private fun configureUI() {
        toolbarText.text = pageTitle
        pdfView.fromBytes(fileData)
                .enableDoubletap(true)
                .defaultPage(0)
                .scrollHandle(null)
                .enableAnnotationRendering(false)
                .enableAntialiasing(false)
                .spacing(0)
                .load()
    }

    private fun shareInvoice() {
        gtmTag?.let {
            KotlinUtils.postOneAppEvent(OneAppEvents.AppScreen.ABSA_SHARE_STATEMENT,OneAppEvents.FeatureName.ABSA)
            Utils.triggerFireBaseEvents(it)
        }
        try {
            cacheFile = File(cacheDir, "$fileName.pdf")

            FileOutputStream(cacheFile).apply {
                write(fileData)
                flush()
                close()
            }
        } catch (e: Exception) {
            Crashlytics.logException(e)
        }


        val uri = cacheFile?.let { FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".WFileProvider", it) }

        val intent = ShareCompat.IntentBuilder.from(this)
                .setType("application/pdf")
                .setStream(uri)
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_CODE_SHARE)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        clearCachedData()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SHARE)
            clearCachedData()
    }

    private fun clearCachedData() {

        if (cacheFile != null && cacheFile?.exists()!!)
            cacheFile?.delete()
    }
}
