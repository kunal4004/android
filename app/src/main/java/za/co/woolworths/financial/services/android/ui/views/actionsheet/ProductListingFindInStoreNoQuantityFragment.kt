package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils
import androidx.annotation.NonNull
import android.content.pm.PackageManager
import com.awfs.coordination.databinding.NoQuantityFindStoreFragmentBinding
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper

class ProductListingFindInStoreNoQuantityFragment(private val mProductListing: IProductListing?) :
    WBottomSheetDialogFragment() {

    private lateinit var binding: NoQuantityFindStoreFragmentBinding
    private var mSkuId: String? = null

    companion object {
        private const val SKU_ID = "SKU_ID"
        const val REQUEST_PERMISSION_LOCATION = 100
        fun newInstance(sku_id: String, mProductListing: IProductListing?) =
            ProductListingFindInStoreNoQuantityFragment(mProductListing).withArgs {
                putString(SKU_ID, sku_id)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mSkuId = getString(SKU_ID, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = NoQuantityFindStoreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Firebase event to be triggered when displaying the out of stock dialog
        FirebaseAnalyticsEventHelper.outOfStock()
        FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location?) {
                activity?.let { activity -> Utils.saveLastLocation(location, activity) }
                FuseLocationAPISingleton.stopLocationUpdate()
                mProductListing?.queryStoreFinderProductByFusedLocation(location)
                dismiss()
            }

            override fun onPopUpLocationDialogMethod() {
            }
        })

        binding.btnNavigateToFindInStore?.setOnClickListener {
            activity?.apply {
                if (!Utils.isLocationEnabled(this)) {
                    Utils.displayValidationMessage(this,
                        CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF,
                        "")
                    return@apply
                }
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSION_LOCATION)
        }
        binding.tvChangeFulfillment?.setOnClickListener {
            dismiss()
            mProductListing?.openChangeFulfillmentScreen()
        }
    }

    private fun startLocationUpdate() = FuseLocationAPISingleton.startLocationUpdate()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray,
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate()
            } else {
                dismiss()
            }

            else -> return
        }
    }
}