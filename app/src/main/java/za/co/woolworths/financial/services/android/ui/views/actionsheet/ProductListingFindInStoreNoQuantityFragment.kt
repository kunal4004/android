package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.no_quantity_find_store_fragment.*
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils
import androidx.annotation.NonNull
import android.content.pm.PackageManager


class ProductListingFindInStoreNoQuantityFragment(private val mProductListing: IProductListing?) : WBottomSheetDialogFragment() {
    private var mSkuId: String? = null

    companion object {
        private const val SKU_ID = "SKU_ID"
        const val REQUEST_PERMISSION_LOCATION = 100
        fun newInstance(sku_id: String, mProductListing: IProductListing?) = ProductListingFindInStoreNoQuantityFragment(mProductListing).withArgs {
            putString(SKU_ID, sku_id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mSkuId = getString(SKU_ID, "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.no_quantity_find_store_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        btnNavigateToFindInStore?.setOnClickListener {
            activity?.apply {
                if (!Utils.isLocationEnabled(this)) {
                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "")
                    return@apply
                }
            }
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
        }
    }

    private fun startLocationUpdate() = FuseLocationAPISingleton.startLocationUpdate()

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
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