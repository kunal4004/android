package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.location.Location
import android.os.Bundle
import android.util.Log
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
import za.co.woolworths.financial.services.android.util.PermissionHelper
import za.co.woolworths.financial.services.android.util.Utils

class ProductListingFindInStoreNoQuantityFragment(private val mProductListing: IProductListing?) : WBottomSheetDialogFragment() {
    private var mSkuId: String? = null
    private var permissionHelper: PermissionHelper? = null
    private var TAG = ProductListingFindInStoreNoQuantityFragment::class.java.simpleName

    companion object {
        private const val SKU_ID = "SKU_ID"
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
            override fun onLocationChange(location: Location) {
                activity?.let { activity -> Utils.saveLastLocation(location, activity) }
                FuseLocationAPISingleton.stopLocationUpdate()
                mProductListing?.queryStoreFinderProductSpecificLocation(location)
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
            permissionHelper?.apply {
                denied {
                    if (it) {
                        Log.e(TAG, "onPermissionDeniedBySystem() called")
                        permissionHelper?.openAppDetailsActivity()
                    } else {
                        Log.e(TAG, "onPermissionDenied() called")
                    }
                }

                requestIndividual {
                    Log.e(TAG, "Individual Permission Granted")
                    startLocationUpdate()
                }


                requestAll {
                    Log.d(TAG, "All permission granted")
                    startLocationUpdate()
                }

            }
        }
    }

    private fun startLocationUpdate() {
        activity?.let { activity -> FuseLocationAPISingleton.startLocationUpdate(activity) }
        dismiss()
    }
}