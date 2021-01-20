package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.link_store_card_process_fragment.*
import kotlinx.android.synthetic.main.npc_card_linked_successful_layout.*
import kotlinx.android.synthetic.main.npc_link_store_card_failure.*
import kotlinx.android.synthetic.main.process_block_card_fragment.incLinkCardSuccessFulView
import kotlinx.android.synthetic.main.process_block_card_fragment.incProcessingTextLayout
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.npc.LinkCardType
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity.Companion.REQUEST_CODE_BLOCK_MY_STORE_CARD
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class LinkStoreCardFragment : AnimatedProgressBarFragment(), View.OnClickListener {

    private var mStoreCardRequest: StoreCardOTPRequest? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null
    private var storeDetails: StoreCardsData? = null
    private var otpMethodType: OTPMethodType? = null
    private var linkStoreCard: LinkStoreCard? = null
    private var mLinkCardType: String? = null
    private var mStoreCardListener: IStoreCardListener? = null

    companion object {
        fun newInstance() = LinkStoreCardFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.link_store_card_process_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLoader()
        linkStoreCardRequest()

        tvCallCenterNumber?.paintFlags = tvCallCenterNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        tvCallCenterNumber?.setOnClickListener(this)
        btnRetryOnFailure?.setOnClickListener(this)
        closeIconImageView?.setOnClickListener(this)
        ibBack?.setOnClickListener(this)
        okGotItButton?.setOnClickListener(this)
        uniqueIdsForLinkStoreCard()
    }

    private fun uniqueIdsForLinkStoreCard() {
        activity?.resources?.apply {
            incProcessingTextLayout?.contentDescription =
                    getString(R.string.process_your_request_text_indicator)
            closeIconImageView?.contentDescription = getString(R.string.close_icon_tapped)
            ibBack?.contentDescription = getString(R.string.back_button_tapped)
            incLinkCardSuccessFulView?.contentDescription = getString(R.string.card_success_layout)
            successTitleTextView?.contentDescription = getString(R.string.success_link_card_title)
            successLinkCardDescriptionTextView?.contentDescription =
                    getString(R.string.success_link_card_description)
            okGotItButton?.contentDescription = getString(R.string.success_got_it_button_tapped)
            incLinkCardFailure?.contentDescription = getString(R.string.link_card_failure_layout)
            failureTitleTextView?.contentDescription =
                    getString(R.string.link_card_failure_title_label)
            failureLinkCardDescriptionTextView?.contentDescription =
                    getString(R.string.link_card_failure_description)
            btnRetryOnFailure?.contentDescription = getString(R.string.retry_on_failure_button)
            tvCallCenterNumber?.contentDescription = getString(R.string.call_center_number_tapped)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.apply {
            try {
                mStoreCardListener = this as? IStoreCardListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }


    private fun linkStoreCardRequest() {

        val mCurrentActivity = getCurrentActivity()

        mCurrentActivity?.apply {

            val sequenceNumber: Int?
            storeDetails = getStoreCardDetail().storeCardsData

            if (activity is InstantStoreCardReplacementActivity) {
                mLinkCardType = LinkCardType.LINK_NEW_CARD.type
                sequenceNumber = 1
            } else {
                sequenceNumber = null
                mLinkCardType = LinkCardType.VIRTUAL_TEMP_CARD.type
            }

            otpMethodType = getOTPMethodType()

            linkStoreCard = LinkStoreCard(storeDetails?.productOfferingId?.toInt()
                    ?: 0, storeDetails?.visionAccountNumber
                    ?: "", getCardNumber(), sequenceNumber, getOtpNumber(), getOTPMethodType(), mLinkCardType!!)
            linkStoreCard?.let { request ->
                mStoreCardRequest = otpMethodType?.let { otp -> StoreCardOTPRequest(this, otp) }
                mStoreCardRequest?.linkStoreCardRequest(object : IOTPLinkStoreCard<LinkNewCardResponse> {
                    override fun showProgress() {
                        super.showProgress()
                        linkStoreCardProgress()
                    }

                    override fun onSuccessHandler(response: LinkNewCardResponse) {
                        super.onSuccessHandler(response)
                        if (!isAdded) return

                        val account = Account()
                        account.accountNumber = storeDetails?.visionAccountNumber
                        account.productOfferingId = storeDetails?.productOfferingId?.toInt() ?: 0
                        when (mLinkCardType) {
                            LinkCardType.LINK_NEW_CARD.type -> {
                                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_LINK_CONFIRM)
                            }
                        }
                        // Make store card call
                        mStoreCardRequest?.getStoreCards(object : IOTPLinkStoreCard<StoreCardsResponse> {

                            override fun onSuccessHandler(response: StoreCardsResponse) {
                                if (!isAdded) return
                                super.onSuccessHandler(response)
                                mStoreCardsResponse = response
                                clearFlag()
                                object : CountDownTimer(1500, 100) {
                                    override fun onTick(millisUntilFinished: Long) {
                                    }

                                    override fun onFinish() {
                                        when (mLinkCardType) {
                                            LinkCardType.LINK_NEW_CARD.type -> {
                                                progressState()?.animateSuccessEnd(true)
                                                linkStoreCardSuccess()
                                            }

                                            LinkCardType.VIRTUAL_TEMP_CARD.type -> {
                                                virtualStoreCardSuccess()
                                                Handler().postDelayed({
                                                    handleStoreCardResponse(response)
                                                }, AppConstant.DELAY_3000_MS)
                                            }
                                        }
                                    }
                                }.start()
                            }

                            // get card failure
                            override fun onFailureHandler() {
                                super.onFailureHandler()
                                onFailure()
                            }
                        }, account)
                    }

                    // link card failure
                    override fun onFailureHandler() {
                        super.onFailureHandler()
                        onFailure()
                    }

                    // OTP Failure View
                    override fun onFailureHandler(response: Response?) {
                        super.onFailureHandler(response)
                        when (response?.code) {
                            "1037" -> {
                                mStoreCardListener?.navigateToPreviousFragment(response.desc)
                                activity?.supportFragmentManager?.apply {
                                    findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
                                }
                            }
                            else -> onFailure()
                        }
                    }


                }, request)
            }
        }

    }

    private fun getCurrentActivity(): MyCardActivityExtension? {
        return when (activity) {
            is InstantStoreCardReplacementActivity -> (activity as? InstantStoreCardReplacementActivity)
            is GetTemporaryStoreCardPopupActivity -> (activity as? GetTemporaryStoreCardPopupActivity)
            else -> null
        }
    }

    private fun virtualStoreCardSuccess() {
        progressState()?.animateSuccessEnd(true)
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
        includeVirtualTempCardSuccessMessage?.visibility = VISIBLE
    }

    private fun onFailure() {
        if (!isAdded) return
        progressState()?.animateSuccessEnd(false)
        object : CountDownTimer(1500, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                linkStoreCardFailure()
            }
        }.start()
    }

    private fun linkStoreCardSuccess() {
        progressState()?.animateSuccessEnd(true)
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = GONE
        incLinkCardSuccessFulView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    private fun linkStoreCardProgress() {
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = GONE
        incLinkCardSuccessFulView?.visibility = GONE
        incProcessingTextLayout?.visibility = VISIBLE
    }

    private fun linkStoreCardFailure() {
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = VISIBLE
        incLinkCardFailure?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.tvCallCenterNumber -> Utils.makeCall( "0861 50 20 20")

                R.id.btnRetryOnFailure -> onAPIFailureRetry()

                R.id.ibBack -> onBackPressed()
                R.id.okGotItButton -> {
                    mStoreCardsResponse?.let { handleStoreCardResponse(it) }
                }
                R.id.closeIconImageView -> {
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun handleStoreCardResponse(storeCardsResponse: StoreCardsResponse) {
        if (!isAdded) return
        activity?.let { activity ->

            val storeCardData = storeCardsResponse.storeCardsData

            val tempStoreCardData =
                    (activity as? MyCardActivityExtension)?.getStoreCardDetail()?.storeCardsData
            val tempProductOfferingId = tempStoreCardData?.productOfferingId
            val tempVisionAccountNumber = tempStoreCardData?.visionAccountNumber

            storeCardData?.visionAccountNumber = tempVisionAccountNumber ?: ""
            storeCardData?.productOfferingId = tempProductOfferingId ?: ""

            storeCardData?.apply {
                if (generateVirtualCard) {
                    val intent = Intent(activity, GetTemporaryStoreCardPopupActivity::class.java)
                    intent.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
                    activity.startActivity(intent)
                    activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                } else {
                    val displayStoreCardDetail = Intent(activity, MyCardDetailActivity::class.java)
                    displayStoreCardDetail.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
                    activity.startActivityForResult(displayStoreCardDetail, REQUEST_CODE_BLOCK_MY_STORE_CARD)
                    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                    activity.setResult(ProcessBlockCardFragment.RESULT_CODE_BLOCK_CODE_SUCCESS)
                }
            }

            activity.finish()
            activity.overridePendingTransition(0, 0)
        }

    }

    private fun onAPIFailureRetry() {
        (activity as? MyCardActivityExtension)?.apply {
            when (mLinkCardType) {
                LinkCardType.LINK_NEW_CARD.type -> {
                    handleStoreCardFailureResponse(getStoreCardDetail())
                }
                LinkCardType.VIRTUAL_TEMP_CARD.type -> {
                    handleStoreCardFailureResponse(getStoreCardDetail())
                }
            }
        }
    }

    /**
     * when link card fails show an error and its back to the store card container page
     * if link passes, and getcards fails, go back to the main account page.
     * From there use can tap "my cards" to redo the getcards call
     */

    private fun handleStoreCardFailureResponse(storeCardsResponse: StoreCardsResponse) {
        if (!isAdded) return
        activity?.let { activity ->

            val storeCardData = storeCardsResponse.storeCardsData

            val tempStoreCardData =
                    (activity as? MyCardActivityExtension)?.getStoreCardDetail()?.storeCardsData
            val tempProductOfferingId = tempStoreCardData?.productOfferingId
            val tempVisionAccountNumber = tempStoreCardData?.visionAccountNumber

            storeCardData?.visionAccountNumber = tempVisionAccountNumber ?: ""
            storeCardData?.productOfferingId = tempProductOfferingId ?: ""

            val linkStoreCardHasFailed = mStoreCardRequest?.linkStoreCardHasFailed
            val getCardCallHasFailed = mStoreCardRequest?.getCardCallHasFailed

            when (mLinkCardType) {
                LinkCardType.LINK_NEW_CARD.type -> {
                    if (linkStoreCardHasFailed!!) {
                        val openLinkNewCardActivity =
                                Intent(activity, InstantStoreCardReplacementActivity::class.java)
                        openLinkNewCardActivity.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
                        activity.startActivityForResult(openLinkNewCardActivity, INSTANT_STORE_CARD_REPLACEMENT_REQUEST_CODE)
                        activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        activity.finish()
                    }

                    if (getCardCallHasFailed!!) {
                        finishActivity(activity)
                    }
                }
                LinkCardType.VIRTUAL_TEMP_CARD.type -> {
                    if (linkStoreCardHasFailed!!) {
                        val intent =
                                Intent(activity, GetTemporaryStoreCardPopupActivity::class.java)
                        intent.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
                        activity.startActivity(intent)
                        activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        activity.finish()
                    }

                    if (getCardCallHasFailed!!) {
                        finishActivity(activity)
                    }
                }
            }
        }
    }

    private fun finishActivity(activity: FragmentActivity) {
        activity.finish()
        activity.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

}