package za.co.woolworths.financial.services.android.ui.vto.ui.gallery

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.databinding.BrowseFileFullScreenActivityBinding
import za.co.woolworths.financial.services.android.ui.vto.ui.gallery.ImageResultContract.Companion.GET_URI
import za.co.woolworths.financial.services.android.ui.vto.ui.gallery.ImageResultContract.Companion.SEND_URI
import za.co.woolworths.financial.services.android.util.pickimagecontract.PickImageFileContract
import android.view.WindowManager
import za.co.woolworths.financial.services.android.util.Utils



class BrowseFullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: BrowseFileFullScreenActivityBinding
    private lateinit var uri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        Utils.updateStatusBarBackground(this)
        binding = BrowseFileFullScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        uri = intent.getParcelableExtra(SEND_URI)!!
        binding.pickedImage.setImageURI(uri)
        val size = ImageResultContract.getImageSizeFromUriInMegaByte(this,uri)
        binding.txtSize.text = "Actual Size ($size MB)"
        binding.chooseImage.setOnClickListener {

            if (size > 100) {
                finish()
            } else {
                val returnedUri = Intent().apply {
                    putExtra(GET_URI, uri)
                }
                setResult(Activity.RESULT_OK, returnedUri)
                finish()
            }

        }
        binding.cancelImage.setOnClickListener {

            pickPhotoFromFile.launch("image/*")
        }


    }
    private val pickPhotoFromFile = registerForActivityResult(PickImageFileContract()) { result ->

        if (null != result) {
            uri=result
            binding.pickedImage.setImageURI(result)
            val size = ImageResultContract.getImageSizeFromUriInMegaByte(this,uri)
            binding.txtSize.text = "Actual Size ($size MB)"
        }

    }


}