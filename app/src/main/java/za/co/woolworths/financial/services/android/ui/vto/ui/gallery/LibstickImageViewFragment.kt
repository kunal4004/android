package za.co.woolworths.financial.services.android.ui.vto.ui.gallery



import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout


import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.awfs.coordination.databinding.VtoImageviewFragmentBinding
import com.perfectcorp.perfectlib.FaceData
import com.perfectcorp.perfectlib.PhotoMakeup
import com.perfectcorp.perfectlib.PhotoMakeup.DetectFaceCallback
import com.perfectcorp.perfectlib.VtoApplier
import com.perfectcorp.perfectlib.VtoApplier.ApplyCallback
import kotlinx.android.synthetic.main.vto_imageview_fragment.*
import za.co.woolworths.financial.services.android.ui.vto.ui.PermissionAction
import za.co.woolworths.financial.services.android.ui.vto.presentation.PermissionViewModel
import za.co.woolworths.financial.services.android.ui.vto.ui.PfSDKInitialCallback
import za.co.woolworths.financial.services.android.ui.vto.ui.SdkUtility
import za.co.woolworths.financial.services.android.ui.vto.utils.PermissionUtil
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

private const val REQUEST_PERMISSION_MEDIA = 100
private const val IMAGE_CHOOSE = 1000;

class LibstickImageViewFragment : Fragment() {

    private lateinit var _binding: VtoImageviewFragmentBinding
    private val binding get() = _binding!!
    private var permissionDenied = false
    private val viewModel: PermissionViewModel by viewModels()
    private var photoMakeup: PhotoMakeup? = null
    private var applier: VtoApplier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)}

    override fun onCreateView(inflater: LayoutInflater, group: ViewGroup?, state: Bundle?): View? {
        viewModel.actions.observe(viewLifecycleOwner, Observer { handleAction(it) })
        _binding = VtoImageviewFragmentBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pfSDKInit()
        // pfSDKTest()
        checkGalleryPermission()
        binding.imgVTOEffect.setOnClickListener {
            clearEffect()
        }
    }

    private fun pfSDKTest() {
        SdkUtility.initSdk(requireContext(), object : PfSDKInitialCallback {
             override fun onInitialized() {
                 Toast.makeText(requireActivity(), "passs", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(throwable: Throwable?) {
                val message = "SDK init failed. throwable=$throwable"
                Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()

            }


        })
    }

    private fun clearEffect() {

        applier?.clearAllEffects(object : ApplyCallback {
            override fun onSuccess(bitmap: Bitmap) {
                setImageBitmap(bitmap)
            }

            override fun onFailure(throwable: Throwable) {

            }

            override fun applyProgress(progress: Double) {

            }
        })
    }

    private fun setImageBitmap(bitmap: Bitmap) {


    }

    private fun checkGalleryPermission() {
        if (!PermissionUtil.hasStoragePermission(requireContext())) {
            if (!permissionDenied) {
                viewModel.requestStoragePermissions()
            }
        } else {
            // open gallery
            chooseImageGallery()
        }
    }

    private fun pfSDKInit() {
        SdkUtility.initSdk(requireContext(), object : PfSDKInitialCallback {
            override fun onInitialized() {
                PhotoMakeup.create(object : PhotoMakeup.CreateCallback {
                    override fun onSuccess(photoMakeup: PhotoMakeup) {
                        this@LibstickImageViewFragment.photoMakeup = photoMakeup

                        VtoApplier.create(photoMakeup, object : VtoApplier.CreateCallback {
                            override fun onSuccess(applierVTO: VtoApplier) {
                                applier = applierVTO

                            }

                            override fun onFailure(throwable: Throwable) {
                                val message =
                                    "ProfileApplier create failed. throwable=$throwable"

                            }
                        })
                    }

                    override fun onFailure(throwable: Throwable) {
                        val message = "PhotoMakeup create failed. throwable=$throwable"

                    }
                })
            }

            override fun onFailure(throwable: Throwable?) {
                val message = "SDK init failed. throwable=$throwable"

            }


        })

    }

    override fun onResume() {
        super.onResume()


    }

    override fun onPause() {
        permissionDenied = false
        super.onPause()
    }

    private fun handleAction(action: PermissionAction) {
        when (action) {
            PermissionAction.StoragePermissionsRequested -> PermissionUtil.requestStoragePermission(
                this,
                REQUEST_PERMISSION_MEDIA
            )
            else -> null
        }
    }

    private fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_CHOOSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
           // binding.imgVTOEffect.setImageURI(data?.data)

            binding.imgVTOEffect.setPhotoUri(Uri.parse(data?.data.toString()))
            loadPhoto(data?.data)
        }

    }

    private fun loadPhoto(imageUri: Uri?) {

        if (imageUri == null) {
            val message = "No valid photo path."
            return
        }
        try {
            requireActivity()!!.contentResolver.openInputStream(imageUri).use { imageStream ->
                val bitmap = BitmapFactory.decodeStream(imageStream)
                val matrix: Matrix =
                    SdkUtility.getRotationMatrixByExif(
                        requireActivity()!!.contentResolver,
                        imageUri
                    )
                val selectedImage =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (bitmap != selectedImage) {
                    bitmap.recycle()
                }
                detectFace(selectedImage)
            }
        } catch (e: Exception) {
        }


    }

    private fun detectFace(selectedImage: Bitmap?) {


        photoMakeup?.detectFace(selectedImage, object : DetectFaceCallback {
            override fun onSuccess(faceList: List<FaceData>) {

                if (faceList.isEmpty()) {

                    return
                }

                // Select a face for applying effects.
                val faceIndex = Random().nextInt(faceList.size)
                val faceData = faceList[faceIndex]
                photoMakeup!!.setFace(faceData)

                // debug code start
                val faceRect = faceData.faceRect
                val scale: Int =
                    if (selectedImage!!.getWidth() / selectedImage!!.getHeight() >= imgVTOEffect.getWidth() / imgVTOEffect.getHeight()) imgVTOEffect.getWidth() / selectedImage.getWidth()  else imgVTOEffect.getHeight() / selectedImage!!.getHeight()
                val faceRectView = View(requireActivity())
                //TODO: remove after test
               // faceRectView.setBackgroundResource(R.drawable.selector_item_border)
                val params = ConstraintLayout.LayoutParams(
                    (scale * (faceRect.right - faceRect.left)).toInt(),
                    (scale * (faceRect.bottom - faceRect.top)).toInt()
                )
                params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                params.leftMargin =
                    (faceRect.left * scale).toInt() + Math.abs(imgVTOEffect.getWidth() - (selectedImage.getWidth() * scale) as Int) / 2
                params.topMargin =
                    (faceRect.top * scale).toInt() + Math.abs(imgVTOEffect.getHeight() - (selectedImage.getHeight() * scale) as Int) / 2

                binding.vtoImageRoot?.addView(faceRectView, params)
                faceRectView.postDelayed(
                    { binding.vtoImageRoot?.removeView(faceRectView) },
                    TimeUnit.SECONDS.toMillis(3)
                )

            }

            override fun onFailure(throwable: Throwable) {

            }
        })

    }

    override fun onRequestPermissionsResult(
        code: Int,
        permission: Array<out String>,
        res: IntArray
    ) {
        when (code) {
            REQUEST_PERMISSION_MEDIA -> {
                when {
                    res.isEmpty() -> {
                        //Do nothing
                    }
                    res[0] == PackageManager.PERMISSION_GRANTED -> {
                        // open gallery
                        chooseImageGallery()
                    }
                    else -> {
                        permissionDenied = true
                        viewModel.requestStoragePermissions()

                    }
                }
            }
        }
    }


    companion object {
        fun newInstance() = LibstickImageViewFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
       // _binding = null
    }

}