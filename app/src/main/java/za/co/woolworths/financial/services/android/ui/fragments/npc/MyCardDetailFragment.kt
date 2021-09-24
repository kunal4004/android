package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.*
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_SENT_TO
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_VALUE
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.ACTIVATE_UNBLOCK_CARD_ON_LANDING
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.NOW
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.TEMPORARY
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.ScanBarcodeToPayDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.TemporaryStoreCardExpireInfoDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.ui.views.snackbar.OneAppSnackbar
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_10_MS
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION
import java.net.SocketTimeoutException
import java.util.*

class MyCardDetailFragment : MyCardExtension(), ScanBarcodeToPayDialogFragment.IOnTemporaryStoreCardDialogDismiss, OnClickListener {

    private var temporaryFreezeCard: TemporaryFreezeStoreCard? = null
    private var mStoreCard: StoreCard? = null
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null
    private var mShouldActivateBlockCardOnLanding : Boolean = false
    private var isFreezeCardCallCompleted : Boolean = true
    private var isUnFreezeCardCallCompleted : Boolean = true

    private enum class AutoConnectStoreCardType{FREEZE, UNFREEZE}

    private var autoConnectStoreCardType : AutoConnectStoreCardType = AutoConnectStoreCardType.FREEZE

    companion object {
        var SHOW_TEMPORARY_FREEZE_DIALOG = false
        var SHOW_BLOCK_CARD_SCREEN = false
        var FREEZE_CARD_DETAIL = false
        var BLOCK_CARD_DETAIL = false

        fun newInstance(storeCardDetail: String?, shouldActivateUnblockCardOnLanding: Boolean) = MyCardDetailFragment().withArgs {
            putString(STORE_CARD_DETAIL, storeCardDetail)
            putBoolean(ACTIVATE_UNBLOCK_CARD_ON_LANDING, shouldActivateUnblockCardOnLanding)
        }

        fun cardName(): String {
            val jwtDecoded: JWTDecodedModel? = SessionUtilities.getInstance().jwt
            val name = jwtDecoded?.name?.get(0) ?: ""
            val familyName = jwtDecoded?.family_name?.get(0) ?: ""
            return "$name $familyName"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
            mShouldActivateBlockCardOnLanding = getBoolean(ACTIVATE_UNBLOCK_CARD_ON_LANDING, false)

            activity?.let {
                Utils.updateStatusBarBackground(it, R.color.grey_bg)
                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                    mStoreCard = mStoreCardsResponse?.storeCardsData?.let { it ->
                        if (isUserGotVirtualCard(it)) it.virtualCard else it.primaryCards?.get(PRIMARY_CARD_POSITION)
                    }
                }
            }
        }

        autoConnectDetector()
    }

    private fun autoConnectDetector() {
        activity?.let {
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(it, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (autoConnectStoreCardType) {
                        AutoConnectStoreCardType.FREEZE -> {
                            if (hasConnection && !isFreezeCardCallCompleted) {
                                temporaryCardFreezeConfirmed()
                            }
                        }
                        AutoConnectStoreCardType.UNFREEZE -> {
                            if (hasConnection && !isUnFreezeCardCallCompleted) {
                                temporaryCardUnFreezeConfirmed()
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        populateView()

        temporaryCardFreezeSwitch?.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (compoundButton.isPressed) {
                when (isChecked) {
                    true -> {
                        linkDeviceIfNecessary({
                            FREEZE_CARD_DETAIL = true
                            temporaryCardFreezeSwitch?.isChecked = false
                        },{
                            temporaryFreezeCard?.showFreezeStoreCardDialog(childFragmentManager)
                        })
                    }
                    false -> temporaryFreezeCard?.showUnFreezeStoreCardDialog(childFragmentManager)
                }
            }
        }

        // call to unblock/unfreeze store Card
        if (mShouldActivateBlockCardOnLanding) {
            temporaryFreezeCard?.unblockStoreCardRequest()
        }
    }

    override fun onResume() {
        super.onResume()
        if(SHOW_TEMPORARY_FREEZE_DIALOG){
            SHOW_TEMPORARY_FREEZE_DIALOG = false
            temporaryCardFreezeSwitch?.isChecked = true
            temporaryFreezeCard?.showFreezeStoreCardDialog(childFragmentManager)
        }
        else if(SHOW_BLOCK_CARD_SCREEN){
            SHOW_BLOCK_CARD_SCREEN = false
            activity?.let { navigateToBlockMyCardActivity(it, mStoreCardDetail) }
        }
    }

    private fun initTemporaryFreezeCard() {
        temporaryFreezeCard = TemporaryFreezeStoreCard(mStoreCardsResponse, object : ITemporaryCardFreeze {

            override fun showProgress() {
                super.showProgress()
                temporaryFreezeCardProgressBar?.visibility = VISIBLE
                temporaryCardFreezeSwitch?.visibility = GONE
            }

            override fun hideProgress() {
                super.hideProgress()
                temporaryFreezeCardProgressBar?.visibility = GONE
                temporaryCardFreezeSwitch?.visibility = VISIBLE
            }

            override fun onFreezeCardSuccess(response: BlockMyCardResponse?) {
                super.onFreezeCardSuccess(response)
                (activity as? MyCardDetailActivity)?.shouldNotifyStateChanged = true
                if (!isAdded) return
                isFreezeCardCallCompleted = true
                hideProgress()
                when (response?.httpCode) {
                    200 -> {
                        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SC_FREEZE_CARD, this) }
                        OneAppSnackbar.make(cardNestedScrollView, bindString(R.string.card_temporarily_frozen_label).toUpperCase(Locale.getDefault())).show()
                        temporaryFreezeCard?.setBlockType(TEMPORARY)
                        temporaryFreezeCard?.showActiveTemporaryFreezeCard(temporaryCardFreezeSwitch, imStoreCard, cardStatus, blockCard)
                    }

                    440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response?.stsParams
                            ?: "", activity)

                    else -> {
                        temporaryCardFreezeSwitch?.isChecked = false
                        activity?.supportFragmentManager?.let { fragmentManager ->
                            Utils.showGeneralErrorDialog(fragmentManager, response?.response?.desc
                                    ?: "")
                        }
                    }
                }
            }

            override fun onUnFreezeSuccess(response: UnblockStoreCardResponse?) {
                super.onUnFreezeSuccess(response)
                if (!isAdded) return
                (activity as? MyCardDetailActivity)?.shouldNotifyStateChanged = true
                isFreezeCardCallCompleted = true
                hideProgress()
                when (response?.httpCode) {
                    200 -> {
                        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SC_UNFREEZE_CARD, this) }
                        temporaryFreezeCard?.setBlockType(NOW)
                        OneAppSnackbar.make(cardNestedScrollView, bindString(R.string.card_temporarily_unfrozen_label).toUpperCase(Locale.getDefault())).show()
                        temporaryCardFreezeSwitch?.isChecked = false
                        temporaryFreezeCard?.showActiveTemporaryFreezeCard(temporaryCardFreezeSwitch, imStoreCard, cardStatus, blockCard)
                    }

                    440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response?.stsParams ?: "", activity)

                    else -> {
                        temporaryCardFreezeSwitch?.isChecked = false
                        activity?.supportFragmentManager?.let { fragmentManager ->
                            Utils.showGeneralErrorDialog(fragmentManager, response?.response?.desc ?: "")
                        }
                    }
                }
            }

            override fun onTemporaryCardFreezeCanceled() {
                super.onTemporaryCardFreezeCanceled()
                temporaryCardFreezeSwitch?.isChecked = false
            }

            override fun onStoreCardFailure(error: Throwable?) {
                super.onStoreCardFailure(error)
                activity?.runOnUiThread {
                    if (error is SocketTimeoutException) {
                        temporaryCardFreezeSwitch?.isChecked = false
                        (activity as? MyCardDetailActivity)?.shouldNotifyStateChanged = true
                        isFreezeCardCallCompleted = false
                        activity?.let { ErrorHandlerView(it).showToast() }
                    } else {
                        hideProgress()
                    }
                }
            }

            override fun onUnFreezeStoreCardFailure(error: Throwable?) {
                super.onUnFreezeStoreCardFailure(error)
                activity?.runOnUiThread {
                    if (error is SocketTimeoutException) {
                        (activity as? MyCardDetailActivity)?.shouldNotifyStateChanged = true
                        isUnFreezeCardCallCompleted = false
                        activity?.let { ErrorHandlerView(it).showToast() }
                    }
                }
            }

            override fun onTemporaryCardFreezeConfirmed() {
                super.onTemporaryCardFreezeConfirmed()
                temporaryCardFreezeConfirmed()
            }

            override fun onTemporaryCardUnFreezeCanceled() {
                super.onTemporaryCardUnFreezeCanceled()
                temporaryCardFreezeSwitch?.isChecked = true
            }

            override fun onTemporaryCardUnFreezeConfirmed() {
                super.onTemporaryCardUnFreezeConfirmed()
                temporaryCardUnFreezeConfirmed()
            }
        })

        temporaryFreezeCard?.showActiveTemporaryFreezeCard(temporaryCardFreezeSwitch, imStoreCard, cardStatus, blockCard)

    }

    private fun temporaryCardUnFreezeConfirmed() {
        autoConnectStoreCardType = AutoConnectStoreCardType.UNFREEZE
        temporaryFreezeCard?.unblockStoreCardRequest()
    }

    private fun temporaryCardFreezeConfirmed() {
        autoConnectStoreCardType = AutoConnectStoreCardType.FREEZE
        temporaryFreezeCard?.blockStoreCardRequest()
    }

    private fun uniqueIdsForCardDetails() {
        cardDetailsView?.contentDescription = bindString(R.string.label_card_details)
        cardNumberLayout?.contentDescription = bindString(R.string.label_cardHolder)
        cardHolderLayout?.contentDescription = bindString(R.string.text_cardHolderName)
        manageView?.contentDescription = bindString(R.string.label_manage_layout)
        blockCard?.contentDescription = bindString(R.string.rlt_BlockCard)
        imStoreCard?.contentDescription = bindString(R.string.store_card_image)
        tvCardHolder?.contentDescription = bindString(R.string.label_cardHolder)
        textViewCardHolderName?.contentDescription = bindString(R.string.text_cardHolderName)
        tvExpires?.contentDescription = bindString(R.string.label_card_expire_date)
        cardExpireDate?.contentDescription = bindString(R.string.text_card_expire_date)
        expireInfo?.contentDescription = bindString(R.string.info_card_expire_date)
        payWithCard?.contentDescription = bindString(R.string.layout_pay_with_card)
        howItWorks?.contentDescription = bindString(R.string.layout_how_it_works)
    }

    private fun initListener() {
        blockCard?.setOnClickListener(this)
        howItWorks?.setOnClickListener(this)
        payWithCard?.setOnClickListener(this)
        expireInfo?.setOnClickListener(this)
    }

    private fun populateView() {
        GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
            mStoreCard?.apply {
                tvCardHolderHeader?.text = this.embossedName
                maskedCardNumberWithSpaces(number).also {
                    textViewCardNumber?.text = it
                    tvCardNumberHeader?.text = it
                }

                toTitleCase(cardName()).also {
                    textViewCardHolderName?.text = it
                }
            }

                //on main thread
            GlobalScope.doAfterDelay(DELAY_10_MS) {
                when (isUserGotVirtualCard(mStoreCardsResponse?.storeCardsData)) {
                    true -> {
                        manageView?.visibility = GONE
                        blockCard?.visibility = GONE
                        temporaryCardFreezeRelativeLayout?.visibility = GONE
                        cardNumberLayout?.visibility = GONE
                        tvCardNumberHeader?.visibility = INVISIBLE
                        cardStatus?.text = getString(R.string.store_card_status_temporary)
                        cardExpireDate?.text =
                            WFormatter.formatDateTOddMMMYYYY(mStoreCard?.expiryDate)
                    }
                    false -> {
                        virtualCardViews?.visibility = GONE
                        cardStatus?.text = getString(R.string.active)
                    }
                }
            }

            uniqueIdsForCardDetails()
            initTemporaryFreezeCard()
        }
    }

    override fun onClick(v: View?) {
        if (isTemporaryCardFreezeInProgress()) return
        when (v?.id) {
            R.id.blockCard -> {
                linkDeviceIfNecessary({
                    BLOCK_CARD_DETAIL = true
                },{
                    activity?.let { navigateToBlockMyCardActivity(it, mStoreCardDetail) }
                })
            }
            R.id.howItWorks -> {
                if (isApiCallInProgress())
                    return
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_HOW_TO, this) }
                activity?.apply {
                    Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                        it.putExtra(HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE, Transition.SLIDE_LEFT)
                        startActivity(it)
                    }
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }
            R.id.payWithCard -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY, this) }
                initPayWithCard()
            }
            R.id.expireInfo -> {
                if (isApiCallInProgress())
                    return
                activity?.supportFragmentManager?.apply {
                    TemporaryStoreCardExpireInfoDialog.newInstance().show((this), TemporaryStoreCardExpireInfoDialog::class.java.simpleName)
                }
            }
        }
    }

    private fun initPayWithCard() {
        when (mStoreCardsResponse?.oneTimePinRequired?.unblockStoreCard) {
            true -> navigateToOTPActivity(OTPMethodType.SMS.name)
            else -> requestUnblockCard()
        }
    }

    private fun requestUnblockCard(otp: String = "") {
        if (isApiCallInProgress())
            return
        showPayWithCardProgressBar(VISIBLE)
        val unblockStoreCardRequestBody = mStoreCard?.let {
            UnblockStoreCardRequestBody(mStoreCardsResponse?.storeCardsData?.visionAccountNumber
                    ?: "", it.number, it.sequence, otp, OTPMethodType.SMS.name)
        }
        unblockStoreCardRequestBody?.let {
            StoreCardAPIRequest().unblockCard(mStoreCardsResponse?.storeCardsData?.productOfferingId
                    ?: "", it, object : IResponseListener<UnblockStoreCardResponse> {
                override fun onSuccess(response: UnblockStoreCardResponse?) {
                    showPayWithCardProgressBar(GONE)
                    when (response?.httpCode) {
                        200 -> displayTemporaryCardToPayDialog()
                        440 -> activity?.let { activity ->
                            response.let {
                                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response?.stsParams
                                        ?: "", activity)
                            }
                        }
                        else -> showErrorDialog(response?.response?.desc
                                ?: bindString(R.string.general_error_desc))
                    }
                }

                override fun onFailure(error: Throwable?) {
                    showPayWithCardProgressBar(GONE)
                    if (error !is SocketTimeoutException)
                        showErrorDialog(bindString(R.string.general_error_desc))
                }
            })
        }
    }

    private fun navigateToOTPActivity(otpSentTo: String?) {
        if (isApiCallInProgress())
            return
        otpSentTo?.let { otpSentTo ->
            activity?.apply {
                val intent = Intent(this, RequestOTPActivity::class.java)
                intent.putExtra(OTP_SENT_TO, otpSentTo)
                startActivityForResult(intent, OTP_REQUEST_CODE)
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
            }
        }
    }

    fun displayTemporaryCardToPayDialog() {
        activity?.apply {
            this@MyCardDetailFragment.childFragmentManager.apply {
                mStoreCardDetail?.let { ScanBarcodeToPayDialogFragment.newInstance(it).show(this, ScanBarcodeToPayDialogFragment::class.java.simpleName) }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == OTP_REQUEST_CODE) {
            val otp = data?.getStringExtra(OTP_VALUE)
            otp?.let { requestUnblockCard(it) }
        }
        //When blocked card and on success My card should refresh
        else if(requestCode == BlockMyCardActivity.REQUEST_CODE_BLOCK_MY_CARD
                && resultCode == MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE) {
            activity?.apply {
                setResult(MyCardDetailActivity.TEMPORARY_FREEZE_STORE_CARD_RESULT_CODE, data)
                finish() // will close previous activity in stack
            }
        }
    }

    override fun onTempStoreCardDialogDismiss() {
        requestBlockCard()
    }

    private fun requestBlockCard() {
        val blockStoreCardRequestBody = mStoreCard?.let {
            BlockCardRequestBody(mStoreCardsResponse?.storeCardsData?.visionAccountNumber
                    ?: "", it.number, it.sequence.toInt(), 6)
        }
        blockStoreCardRequestBody?.let {
            StoreCardAPIRequest().blockCard(mStoreCardsResponse?.storeCardsData?.productOfferingId
                    ?: "", it, object : IResponseListener<BlockMyCardResponse> {
                override fun onSuccess(response: BlockMyCardResponse?) {
                }

                override fun onFailure(error: Throwable?) {
                }
            })
        }
    }

    private fun showPayWithCardProgressBar(state: Int) {
        activity?.apply {
            payWithCardTokenProgressBar?.indeterminateDrawable?.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            payWithCardTokenProgressBar?.visibility = state
            payWithCardNextArrow?.visibility = if (state == VISIBLE) GONE else VISIBLE
        }
    }

    fun showErrorDialog(errorMessage: String) {
        val dialog = ErrorDialogFragment.newInstance(errorMessage)
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    private fun isUserGotVirtualCard(storeCardsData: StoreCardsData?): Boolean {
        // virtual card should not be blocked.
        return (storeCardsData?.virtualCard != null && WoolworthsApplication.getVirtualTempCard()?.isEnabled == true && !TemporaryFreezeStoreCard.PERMANENT.equals(storeCardsData?.virtualCard?.blockType, ignoreCase = true))
    }

    private fun isApiCallInProgress(): Boolean {
        return payWithCardTokenProgressBar?.visibility == VISIBLE
    }

    fun isTemporaryCardFreezeInProgress() = temporaryFreezeCardProgressBar?.visibility == VISIBLE

    private fun linkDeviceIfNecessary(doJob: () -> Unit, elseJob: () -> Unit){
        if (!MyAccountsFragment.verifyAppInstanceId() && Utils.isGooglePlayServicesAvailable()) {
            doJob()
            activity?.let {
                val intent = Intent(it, LinkDeviceConfirmationActivity::class.java)
                intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, ApplyNowState.STORE_CARD)
                it.startActivity(intent)
                it.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
            }
        }
        else{
            elseJob()
        }
    }
}