package za.co.woolworths.financial.services.android.ui.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.databinding.ProductMultipleImagesBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.onecartgetstream.chat.ChatFragment.Companion.AUXILIARY_IMAGE
import za.co.woolworths.financial.services.android.util.Utils

class MultipleImageActivity : AppCompatActivity() {

    private lateinit var binding: ProductMultipleImagesBinding
    private var mAuxiliaryImages: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        binding = ProductMultipleImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getBundle()
        initView()
        setImage()
    }

    private fun getBundle() {
        mAuxiliaryImages = intent?.extras?.getString(AUXILIARY_IMAGE, "") ?: ""
    }

    private fun initView() {
        binding.imClose.setOnClickListener {
            closeView()
        }
    }

    private fun setImage() {
        binding.imProductView?.setPhotoUri(Uri.parse(mAuxiliaryImages))
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this,
            FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL_IMAGE_ZOOM)
    }

    override fun onBackPressed() {
        closeView()
    }

    private fun closeView() {
        finish()
        overridePendingTransition(0, 0)
    }
}