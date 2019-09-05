package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.replace_card_fragment.*
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils

@RequiresApi(Build.VERSION_CODES.M)
class GetReplacementCardFragment : MyCardExtension() {

    companion object {
        fun newInstance() = GetReplacementCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.replace_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it) }
        updateToolbarBg()
        tvAlreadyHaveCard?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        requestGPSLocation()
        tvAlreadyHaveCard?.setOnClickListener { (activity as? AppCompatActivity)?.apply { navigateToLinkNewCardActivity(this, "") } }
        btnParticipatingStores?.setOnClickListener { navigateToParticipatingStores() }
    }

    private fun requestGPSLocation() {
        FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location) {
                activity?.let { activity -> Utils.saveLastLocation(location, activity) }
                FuseLocationAPISingleton.stopLocationUpdate()
                Log.e("locationRequestApp", location.toString())
            }

            override fun onPopUpLocationDialogMethod() {
            }
        })
    }

    private fun updateToolbarBg() {
        (activity as? MyCardDetailActivity)?.apply {
            hideToolbarTitle()
            changeToolbarBackground(R.color.white)
        }
    }

    private fun navigateToParticipatingStores() {
        activity?.apply {

            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this)) {
                Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "")
                return@apply
            }

            // If location services enabled, request v4/user/locations
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ProductListingFindInStoreNoQuantityFragment.REQUEST_PERMISSION_LOCATION)

//            val location = ""
//            val listType = object : TypeToken<List<StoreDetails>>() {}.type
//            WoolworthsApplication.getInstance().wGlobalState.storeDetailsArrayList = Gson().fromJson(location, listType)
//            val intentInStoreFinder = Intent(this, WStockFinderActivity::class.java)
//            intentInStoreFinder.putExtra("PRODUCT_NAME", getString(R.string.participating_stores))
//            intentInStoreFinder.putExtra("CONTACT_INFO", getString(R.string.participating_store_desc))
//
//            startActivity(intentInStoreFinder)
//            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)

        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            ProductListingFindInStoreNoQuantityFragment.REQUEST_PERMISSION_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate()
            }
            else -> return
        }
    }

    private fun startLocationUpdate() = activity?.let { activity -> FuseLocationAPISingleton.startLocationUpdate(activity) }

}