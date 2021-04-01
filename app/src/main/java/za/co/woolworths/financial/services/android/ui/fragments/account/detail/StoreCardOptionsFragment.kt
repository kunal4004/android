package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.NonNull
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_cart_item.*
import kotlinx.android.synthetic.main.account_detail_header_fragment.*
import kotlinx.android.synthetic.main.account_options_layout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.cancelRetrofitRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.detail.card.AccountsOptionFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.views.actionsheet.EnableLocationSettingsFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductListingFindInStoreNoQuantityFragment.Companion.REQUEST_PERMISSION_LOCATION
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.StoreCardViewType

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

        listeners()

    }

    private fun listeners() {
        includeManageMyCard?.apply {
            setOnClickListener(this@StoreCardOptionsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        cardDetailImageView?.apply {
            setOnClickListener(this@StoreCardOptionsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        includeLinkNewCard?.apply {
            setOnClickListener(this@StoreCardOptionsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        AnimationUtilExtension.animateViewPushDown(includeManageMyCard)
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
        if ((activity as? AccountSignedInActivity)?.mAccountSignedInPresenter?.isAccountInDelinquencyMoreThan6Months() == true) return
        lifecycleScope.launch {
            delay(100L)
            FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
                override fun onLocationChange(location: Location?) {
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
                onFreezeUnfreezeStoreCard()
                setStoreCardTag()
            }
            440 -> activity?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, storeCardResponse.response?.stsParams, it) }

            else -> {
                val desc = storeCardResponse.response?.desc ?: ""
                Utils.showGeneralErrorDialog(activity, desc)
            }
        }
    }

    private fun setStoreCardTag() {
        when {
            // Temporary card
            (mCardPresenterImpl?.isVirtualCardEnabled() == true) -> {
                storeCardTagTextView?.text = bindString(R.string.temp_card)
                storeCardTagTextView?.let { KotlinUtils.roundCornerDrawable(it, bindString(R.string.orange_tag)) }
                storeCardTagTextView?.visibility = VISIBLE
                myCardDetailTextView?.visibility = GONE
                manageLinkNewCardGroup?.visibility = GONE
                manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                cardDetailImageView?.alpha = 0.3f
            }
            // Get replacement card
            mCardPresenterImpl?.isReplacementCardAndVirtualCardViewEnabled() != true -> {
                storeCardTagTextView?.text = bindString(R.string.inactive)
                storeCardTagTextView?.let { KotlinUtils.roundCornerDrawable(it, bindString(R.string.red_tag)) }
                storeCardTagTextView?.visibility = VISIBLE
                myCardDetailTextView?.visibility = GONE
                manageLinkNewCardGroup?.visibility = VISIBLE
                manageMyCardTextView?.text = bindString(R.string.replacement_card_label)
                cardDetailImageView?.alpha = 0.3f
            }
            // Temporary card
            mCardPresenterImpl?.isReplacementCardAndVirtualCardViewEnabled() == true -> {
                //when virtual card is disabled on mobile config server
                if (WoolworthsApplication.getInstantCardReplacement()?.isEnabled == false) {
                    storeCardTagTextView?.visibility = GONE
                    myCardDetailTextView?.visibility = VISIBLE
                    manageLinkNewCardGroup?.visibility = GONE
                    manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                    cardDetailImageView?.alpha = 1.0f
                    return
                }
                if (mCardPresenterImpl?.isVirtualCardEnabled() == false) {
                    storeCardTagTextView?.visibility = GONE
                    myCardDetailTextView?.visibility = VISIBLE
                    manageLinkNewCardGroup?.visibility = GONE
                    manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                    cardDetailImageView?.alpha = 1.0f
                    return
                }
                storeCardTagTextView?.text = bindString(R.string.temp_card)
                storeCardTagTextView?.let { KotlinUtils.roundCornerDrawable(it, bindString(R.string.orange_tag)) }
                storeCardTagTextView?.visibility = VISIBLE
                myCardDetailTextView?.visibility = GONE
                manageLinkNewCardGroup?.visibility = GONE
                manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                cardDetailImageView?.alpha = 0.3f
            }
            else -> {
                // Manage your card
                storeCardTagTextView?.visibility = GONE
                myCardDetailTextView?.visibility = VISIBLE
                manageLinkNewCardGroup?.visibility = GONE
                manageMyCardTextView?.text = bindString(R.string.manage_my_card_title)
                cardDetailImageView?.alpha = 1.0f
            }
        }
    }

    private fun onFreezeUnfreezeStoreCard() {
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

    override fun onClick(v: View?) {
        super.onClick(v)
        mCardPresenterImpl?.apply {
            when (v?.id) {
                R.id.includeManageMyCard, R.id.cardDetailImageView -> {
                    if (loadStoreCardProgressBar?.visibility == VISIBLE) return
                    cancelRetrofitRequest(mOfferActiveCall)

                    when (manageMyCardTextView?.text?.toString()) {
                        bindString(R.string.replacement_card_label) -> {
                            getStoreCardResponse()?.let { MyAccountsScreenNavigator.navigateToMyCardDetailActivity(activity, it, screenType = StoreCardViewType.GET_REPLACEMENT_CARD) }
                        }
                        else -> navigateToTemporaryStoreCard()

                    }
                }

                R.id.includeLinkNewCard -> {
                    activity?.runOnUiThread {
                        when (manageMyCardTextView?.text?.toString()) {
                            bindString(R.string.replacement_card_label) -> {
                                val storeCardResponse = getStoreCardResponse()
                                MyAccountsScreenNavigator.navigateToLinkNewCardActivity(activity, Utils.objectToJson(storeCardResponse))
                            }
                        }
                    }
                }
            }
        }
    }

}