package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment.Companion.REQUEST_PERMISSION_LOCATION
import za.co.woolworths.financial.services.android.util.*

class StoreCardOptionsFragment : AccountsOptionFragment() {

    private var accountStoreCardCallWasCompleted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cardDetailImageView?.setImageResource(R.drawable.w_store_card)

        autoConnectListener()

        if (mCardPresenterImpl?.isDebitOrderActive() == VISIBLE) {
            debitOrderViewGroup?.visibility = VISIBLE
            KotlinUtils.roundCornerDrawable(debitOrderIsActiveTextView, "#bad110")
        } else {
            debitOrderViewGroup?.visibility = GONE
        }
        debitOrderViewGroup?.visibility = mCardPresenterImpl?.isDebitOrderActive() ?: 0
    }

    private fun autoConnectListener() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    if (hasConnection && !accountStoreCardCallWasCompleted) {
                        navigateToGetStoreCard()
                    }
                }
            })
        }
    }

    private fun navigateToGetStoreCard() {
        lifecycleScope.launch {
            delay(100L)
            FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
                override fun onLocationChange(location: Location?) {
                        Log.e("myLocationUpdate", Gson().toJson(location))
                        activity?.let { context -> Utils.saveLastLocation(location, context) }
                        FuseLocationAPISingleton.stopLocationUpdate()
                        navigateToGetStoreCards()
                }
            })

        }
        checkForLocationPermission()
    }

    override fun showOnStoreCardFailure(error: Throwable?) {
        activity?.let { ErrorHandlerView(it).showToast() }
    }


    override fun handleStoreCardCardsSuccess(storeCardResponse: StoreCardsResponse) {
        super.handleStoreCardCardsSuccess(storeCardResponse)
        hideStoreCardProgress()
        accountStoreCardCallWasCompleted = true

        when (storeCardResponse.httpCode) {
            200 -> {
                when (mCardPresenterImpl?.getStoreCardBlockType()) {
                    true -> {
                        cardDetailImageView?.setImageDrawable(bindDrawable(R.drawable.card_freeze))
                        manageMyCardTextView?.text = bindString(R.string.unfreeze_my_card_label)
                        tempFreezeTextView?.let { KotlinUtils.roundCornerDrawable(it, "#FF7000") }
                        tempFreezeTextView?.text = bindString(R.string.freeze_temp_label)
                        tempFreezeTextView?.visibility = VISIBLE
                        myCardDetailTextView?.visibility = GONE
                    }
                    else -> {
                        cardDetailImageView?.setImageDrawable(bindDrawable(R.drawable.w_store_card))
                        manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                        tempFreezeTextView?.visibility = GONE
                        myCardDetailTextView?.visibility = VISIBLE
                    }
                }
            }
            440 -> activity?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, storeCardResponse.response?.stsParams, it) }

            else -> {
                val desc = storeCardResponse.response?.desc ?: ""
                Utils.showGeneralErrorDialog(activity, desc)
            }
        }
    }

    override fun showUnBlockStoreCardCardDialog() {
        val storeCardResponse = mCardPresenterImpl?.getStoreCardResponse()
        val temporaryFreezeStoreCard = TemporaryFreezeStoreCard(storeCardResponse, object : ITemporaryCardFreeze {

            override fun onTemporaryCardUnFreezeConfirmed() {
                super.onTemporaryCardUnFreezeConfirmed()
                mCardPresenterImpl?.navigateToMyCardDetailActivity(true)
            }
        })

        temporaryFreezeStoreCard.showUnFreezeStoreCardDialog(childFragmentManager)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE) {
            navigateToGetStoreCard()
        }
        if (requestCode == EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE) {
            activity?.runOnUiThread {
                checkForLocationPermission()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FuseLocationAPISingleton.startLocationUpdate()
            } else {
                KotlinUtils.openApplicationSettings(EnableLocationSettingsFragment.ACCESS_MY_LOCATION_REQUEST_CODE, activity)
            }
            else -> return
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun checkForLocationPermission() {
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_STORES)
        activity?.apply {
            //Check if user has location services enabled. If not, notify user as per current store locator functionality.
            if (!Utils.isLocationEnabled(this)) {
                val enableLocationSettingsFragment: EnableLocationSettingsFragment? = EnableLocationSettingsFragment()
                enableLocationSettingsFragment?.show(supportFragmentManager, EnableLocationSettingsFragment::class.java.simpleName)
                return@apply
            }

            // If location services enabled, extract latitude and longitude
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
        }
    }

}