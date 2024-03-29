package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MyCardFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.contracts.ITemporaryCardFreeze
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.npc.Transition
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.*
import za.co.woolworths.financial.services.android.ui.activities.card.BlockMyCardActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_SENT_TO
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_VALUE
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.ACTIVATE_UNBLOCK_CARD_ON_LANDING
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.NOW
import za.co.woolworths.financial.services.android.ui.fragments.account.freeze.TemporaryFreezeStoreCard.Companion.TEMPORARY
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.FREEZE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.PAY_WITH_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_PAY_WITH_CARD_SCREEN
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.StoreCardActivity.Companion.SHOW_TEMPORARY_FREEZE_DIALOG
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.ScanBarcodeToPayDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.TemporaryStoreCardExpireInfoDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.ui.views.snackbar.OneAppSnackbar
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.Utils.PRIMARY_CARD_POSITION
import java.net.SocketTimeoutException
import java.util.*

class MyCardDetailFragment : MyCardExtension(R.layout.my_card_fragment), ScanBarcodeToPayDialogFragment.IOnTemporaryStoreCardDialogDismiss, OnClickListener {

    private lateinit var binding: MyCardFragmentBinding
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
        setupCard()
        autoConnectDetector()
    }

    private fun setupCard() {
        arguments?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
            mShouldActivateBlockCardOnLanding = getBoolean(ACTIVATE_UNBLOCK_CARD_ON_LANDING, false)

            activity?.let {
                Utils.updateStatusBarBackground(it, R.color.grey_bg)
                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                    mStoreCard = mStoreCardsResponse?.storeCardsData?.let { it ->
                        if (isUserGotVirtualCard(it)) it.virtualCard else it.primaryCards?.get(
                            PRIMARY_CARD_POSITION
                        )
                    }
                }
            }
        }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MyCardFragmentBinding.bind(view)

        binding.initListener()
        binding.populateView()

        binding.temporaryCardFreezeSwitch?.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (compoundButton.isPressed) {
                when (isChecked) {
                    true -> {
                        KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
                            FREEZE_CARD_DETAIL = true
                            binding.temporaryCardFreezeSwitch?.isChecked = false
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
        when {
            SHOW_TEMPORARY_FREEZE_DIALOG -> {
                SHOW_TEMPORARY_FREEZE_DIALOG = false
                binding.temporaryCardFreezeSwitch?.isChecked = true
                temporaryFreezeCard?.showFreezeStoreCardDialog(childFragmentManager)
            }

            SHOW_PAY_WITH_CARD_SCREEN -> {
                SHOW_PAY_WITH_CARD_SCREEN = false
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY, this) }
                initPayWithCard()
            }
        }
    }

    private fun MyCardFragmentBinding.initTemporaryFreezeCard() {
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
                        OneAppSnackbar.make(cardNestedScrollView, bindString(R.string.card_temporarily_frozen_label).uppercase()).show()
                        temporaryFreezeCard?.setBlockType(TEMPORARY)
                        temporaryFreezeCard?.showActiveTemporaryFreezeCard(
                            temporaryCardFreezeSwitch,
                            imStoreCard,
                            cardStatus,
                            blockCard,
                            isUserGotVirtualCard(mStoreCardsResponse?.storeCardsData)
                        )
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
                        temporaryFreezeCard?.showActiveTemporaryFreezeCard(
                            temporaryCardFreezeSwitch,
                            imStoreCard,
                            cardStatus,
                            blockCard,
                            isUserGotVirtualCard(mStoreCardsResponse?.storeCardsData)
                        )
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

        temporaryFreezeCard?.showActiveTemporaryFreezeCard(
            temporaryCardFreezeSwitch,
            imStoreCard,
            cardStatus,
            blockCard,
            isUserGotVirtualCard(mStoreCardsResponse?.storeCardsData)
        )

    }

    private fun temporaryCardUnFreezeConfirmed() {
        autoConnectStoreCardType = AutoConnectStoreCardType.UNFREEZE
        temporaryFreezeCard?.unblockStoreCardRequest()
    }

    private fun temporaryCardFreezeConfirmed() {
        autoConnectStoreCardType = AutoConnectStoreCardType.FREEZE
        temporaryFreezeCard?.blockStoreCardRequest()
    }

    private fun MyCardFragmentBinding.uniqueIdsForCardDetails() {
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

    private fun MyCardFragmentBinding.initListener() {
        blockCard?.setOnClickListener(this@MyCardDetailFragment)
        howItWorks?.setOnClickListener(this@MyCardDetailFragment)
        payWithCard?.setOnClickListener(this@MyCardDetailFragment)
        expireInfo?.setOnClickListener(this@MyCardDetailFragment)
    }

    private fun MyCardFragmentBinding.populateView() {
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

        uniqueIdsForCardDetails()
        if(temporaryCardFreezeRelativeLayout?.visibility == VISIBLE) {
            initTemporaryFreezeCard()
        }
    }

    override fun onClick(v: View?) {
        if (isTemporaryCardFreezeInProgress()) return
        when (v?.id) {
            R.id.howItWorks -> {
                if (isApiCallInProgress())
                    return
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_HOW_TO, this) }
                activity?.apply {
                    Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                        it.putExtra(HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE, Transition.SLIDE_LEFT)
                        mStoreCardsResponse?.apply {
                            if(isUserGotVirtualCard(storeCardsData) &&
                                storeCardsData != null &&
                                storeCardsData?.isStaffMember == true &&
                                storeCardsData?.virtualCardStaffMemberMessage != null){
                                it.putExtra(HowToUseTemporaryStoreCardActivity.STAFF_DISCOUNT_INFO, storeCardsData?.virtualCardStaffMemberMessage)
                            }
                        }
                        startActivity(it)
                    }
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }
            R.id.payWithCard -> {
                KotlinUtils.linkDeviceIfNecessary(activity, ApplyNowState.STORE_CARD, {
                    PAY_WITH_CARD_DETAIL = true
                },{
                    activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY, this) }
                    initPayWithCard()
                })
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
        binding.showPayWithCardProgressBar(VISIBLE)
        val unblockStoreCardRequestBody = mStoreCard?.let {
            UnblockStoreCardRequestBody(mStoreCardsResponse?.storeCardsData?.visionAccountNumber
                    ?: "", it.number, it.sequence.toString(), otp, OTPMethodType.SMS.name)
        }
        unblockStoreCardRequestBody?.let {
            StoreCardAPIRequest().unblockCard(mStoreCardsResponse?.storeCardsData?.productOfferingId
                    ?: "", it, object : IResponseListener<UnblockStoreCardResponse> {
                override fun onSuccess(response: UnblockStoreCardResponse?) {
                    binding.showPayWithCardProgressBar(GONE)
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
                    binding.showPayWithCardProgressBar(GONE)
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
                    ?: "", it.number, it.sequence, 6)
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

    private fun MyCardFragmentBinding.showPayWithCardProgressBar(state: Int) {
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
        return (storeCardsData?.virtualCard != null && AppConfigSingleton.virtualTempCard?.isEnabled == true && !TemporaryFreezeStoreCard.PERMANENT.equals(storeCardsData?.virtualCard?.blockType, ignoreCase = true))
    }

    private fun isApiCallInProgress(): Boolean {
        return binding.payWithCardTokenProgressBar?.visibility == VISIBLE
    }

    fun isTemporaryCardFreezeInProgress() = binding.temporaryFreezeCardProgressBar?.visibility == VISIBLE
}