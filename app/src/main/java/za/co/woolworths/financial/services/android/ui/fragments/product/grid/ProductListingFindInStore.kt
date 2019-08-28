package za.co.woolworths.financial.services.android.ui.fragments.product.grid

import android.app.Activity
import android.location.Location
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils

class ProductListingFindInStore(private val activity: Activity?) {
    private var mFuseLocationAPISingleton: FuseLocationAPISingleton? = FuseLocationAPISingleton

    fun startLocationUpdate() = mFuseLocationAPISingleton?.apply {
        addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location) {

            }

            override fun onPopUpLocationDialogMethod() {

            }
        })

        activity?.let { activity -> startLocationUpdate(activity) }
    }

    // stop location updates
    fun stopLocationUpdate() = mFuseLocationAPISingleton?.stopLocationUpdate()

    fun make() {
        activity?.apply {
            if (Utils.isLocationEnabled(this)) {

            }
        }
    }
}