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
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.*
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.*
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_SENT_TO
import za.co.woolworths.financial.services.android.ui.activities.store_card.RequestOTPActivity.Companion.OTP_VALUE
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.ScanBarcodeToPayDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.TemporaryStoreCardExpireInfoDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ErrorDialogFragment
import za.co.woolworths.financial.services.android.util.StoreCardAPIRequest
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class MyCardDetailFragment : MyCardExtension(), ScanBarcodeToPayDialogFragment.IOnTemporaryStoreCardDialogDismiss, OnClickListener {

    private var mStoreCard: StoreCard? = null
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null

    companion object {
        fun newInstance(storeCardDetail: String?) = MyCardDetailFragment().withArgs {
            putString(STORE_CARD_DETAIL, storeCardDetail)
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

            activity?.let {
                Utils.updateStatusBarBackground(it, R.color.grey_bg)
                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                    mStoreCard = mStoreCardsResponse?.storeCardsData?.let { it ->
                        if (isUserGotVirtualCard(it)) it.virtualCard else it.primaryCards?.get(0)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        populateView()
    }

    private fun initListener() {
        blockCard.setOnClickListener(this)
        howItWorks.setOnClickListener(this)
        payWithCard.setOnClickListener(this)
        expireInfo.setOnClickListener(this)
    }

    private fun populateView() {
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
                blockCardViews.visibility = GONE
                tvCardNumberHeader.visibility = INVISIBLE
                cardStatus.text = getString(R.string.store_card_status_temporay)
                cardExpireDate.text = WFormatter.formatDateTOddMMMMYYYY(mStoreCard?.expiryDate)
            }
            false -> {
                virtualCardViews.visibility = GONE
                cardStatus.text = getString(R.string.active)
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.blockCard -> activity?.let { navigateToBlockMyCardActivity(it, mStoreCardDetail) }
            R.id.howItWorks -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_HOW_TO)
                activity?.apply {
                    Intent(this, HowToUseTemporaryStoreCardActivity::class.java).let {
                        it.putExtra(HowToUseTemporaryStoreCardActivity.TRANSACTION_TYPE, Transition.SLIDE_LEFT)
                        startActivity(it)
                    }
                    overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }
            R.id.payWithCard -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_PAY)
                initPayWithCard()
            }
            R.id.expireInfo -> {
                activity?.supportFragmentManager?.apply {
                    TemporaryStoreCardExpireInfoDialog.newInstance().show((this), TemporaryStoreCardExpireInfoDialog::class.java.simpleName)
                }
            }
        }
    }


    private fun initPayWithCard() {
        when (mStoreCardsResponse?.oneTimePinRequired?.unblockStoreCard) {
            true -> {
                navigateToOTPActivity(OTPMethodType.SMS.name)
            }
            else -> {
                requestUnblockCard()
            }
        }
    }


    private fun requestUnblockCard(otp: String = "") {
        showPayWithCardProgressBar(VISIBLE)
        val unblockStoreCardRequestBody = mStoreCard?.let {
            UnblockStoreCardRequestBody(mStoreCardsResponse?.storeCardsData?.visionAccountNumber
                    ?: "", it.number, it.sequence, otp, OTPMethodType.SMS.name)
        }
        unblockStoreCardRequestBody?.let {
            StoreCardAPIRequest().unblockCard(mStoreCardsResponse?.storeCardsData?.productOfferingId
                    ?: "", it, object : RequestListener<UnblockStoreCardResponse> {
                override fun onSuccess(response: UnblockStoreCardResponse?) {
                    showPayWithCardProgressBar(GONE)
                    when (response?.httpCode) {
                        200 -> displayTemporaryCardToPayDialog()
                        440 -> activity?.let { activity -> response.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response?.stsParams?: "", activity) } }
                        else ->showErrorDialog(response?.response?.desc ?: getString(R.string.general_error_desc))
                    }
                }

                override fun onFailure(error: Throwable?) {
                    showPayWithCardProgressBar(GONE)
                    showErrorDialog(getString(R.string.general_error_desc))
                }
            })
        }
    }

    private fun navigateToOTPActivity(otpSentTo: String?) {
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
        this.childFragmentManager.apply {
            mStoreCardDetail?.let { ScanBarcodeToPayDialogFragment.newInstance(it).show(this, ScanBarcodeToPayDialogFragment::class.java.simpleName) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == OTP_REQUEST_CODE) {
            val otp = data?.getStringExtra(OTP_VALUE)
            otp?.let { requestUnblockCard(it) }
        }
    }

    override fun onDialogDismiss() {
        requestBlockCard()
    }

    override fun onRegenerateBarcode() {
        initPayWithCard()
    }

    private fun requestBlockCard() {
        val blockStoreCardRequestBody = mStoreCard?.let {
            BlockCardRequestBody(mStoreCardsResponse?.storeCardsData?.visionAccountNumber
                    ?: "", it.number, it.sequence.toInt(), 6)
        }
        blockStoreCardRequestBody?.let {
            StoreCardAPIRequest().blockCard(mStoreCardsResponse?.storeCardsData?.productOfferingId
                    ?: "", it, object : RequestListener<BlockMyCardResponse> {
                override fun onSuccess(response: BlockMyCardResponse?) {
                }

                override fun onFailure(error: Throwable?) {
                }
            })
        }
    }

    private fun showPayWithCardProgressBar(state: Int) {
        activity?.apply {
            payWithCardTokenProgressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN)
            payWithCardTokenProgressBar.visibility = state
            payWithCardNextArrow.visibility = if (state == VISIBLE) GONE else VISIBLE
        }
    }

    fun showErrorDialog(errorMessage: String) {
            val dialog = ErrorDialogFragment.newInstance(errorMessage)
            (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()?.let { fragmentTransaction -> dialog.show(fragmentTransaction, ErrorDialogFragment::class.java.simpleName) }
    }

    private fun isUserGotVirtualCard(storeCardsData: StoreCardsData?): Boolean{
        return (storeCardsData?.virtualCard != null && WoolworthsApplication.getVirtualTempCard()?.isEnabled == true)
    }
}