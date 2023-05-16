package za.co.woolworths.financial.services.android.ui.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.databinding.ActivityOrederTaxInvoiceBinding
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.OneAppEvents
import za.co.woolworths.financial.services.android.util.PermissionResultCallback
import za.co.woolworths.financial.services.android.util.PermissionUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.File
import java.io.FileOutputStream

class WPdfViewerActivity : AppCompatActivity(), PermissionResultCallback {

    private lateinit var binding: ActivityOrederTaxInvoiceBinding
    private var pageTitle: String? = null
    private var fileName: String? = null
    private var fileData: ByteArray? = null
    private var cacheFile: File? = null
    private var gtmTag: String? = null
    private var permissionUtils: PermissionUtils? = null
    private var permissions: ArrayList<String> = arrayListOf()

    companion object {
        const val FILE_NAME = "FILE_NAME"
        const val FILE_VALUE = "FILE_VALUE"
        const val PAGE_TITLE = "PAGE_TITLE"
        const val GTM_TAG = "GTM_TAG"
        const val REQUEST_CODE_SHARE = 111
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrederTaxInvoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundleArgument()
        //https://wigroup2.atlassian.net/browse/WOP-6922
        Utils.updateStatusBarBackground(this)

        binding.initView()

        permissionUtils = PermissionUtils(this, this)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    private fun getBundleArgument() {
        intent?.extras?.apply {
            pageTitle = getString(PAGE_TITLE)
            fileName = getString(FILE_NAME)
            fileData = getByteArray(FILE_VALUE)
            gtmTag = getString(GTM_TAG)
        }
    }

    private fun ActivityOrederTaxInvoiceBinding.initView() {
        done.setOnClickListener { onBackPressed() }
        share.setOnClickListener { checkPermissionBeforeSharing() }
        configureUI()
    }

    private fun ActivityOrederTaxInvoiceBinding.configureUI() {
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
            Utils.triggerFireBaseEvents(it, this)
        }
        try {
            cacheFile = File(cacheDir, "$fileName.pdf")

            FileOutputStream(cacheFile).apply {
                write(fileData)
                flush()
                close()
            }
        } catch (e: Exception) {
            FirebaseManager.logException(e)
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

    private fun checkPermissionBeforeSharing() {
        permissionUtils?.checkPermission(
            permissions,
            "Explain here why the app needs permissions",
            1
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun permissionGranted(request_code: Int) {
        shareInvoice()
    }
}
