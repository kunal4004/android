package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.app.Activity
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
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LinkStoreCardProcessFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.npc.LinkCardType
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardActivityExtension
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils
import kotlinx.android.synthetic.main.npc_virtual_temp_card_staff_layout.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.IProgressAnimationState
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE
import za.co.woolworths.financial.services.android.ui.extension.addFragment
import za.co.woolworths.financial.services.android.ui.extension.findFragmentByTag

class LinkStoreCardFragment : MyCardExtension(R.layout.link_store_card_process_fragment), View.OnClickListener,
    IProgressAnimationState {

    private lateinit var binding: LinkStoreCardProcessFragmentBinding
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LinkStoreCardProcessFragmentBinding.bind(view)

        binding.apply {
            showLoader()
            linkStoreCardRequest()

            incLinkCardFailure.tvCallCenterNumber?.paintFlags =
                incLinkCardFailure.tvCallCenterNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            incLinkCardFailure.tvCallCenterNumber?.setOnClickListener(this@LinkStoreCardFragment)
            incLinkCardFailure.btnRetryOnFailure?.setOnClickListener(this@LinkStoreCardFragment)
            closeIconImageView?.setOnClickListener(this@LinkStoreCardFragment)
            ibBack?.setOnClickListener(this@LinkStoreCardFragment)
            incLinkCardSuccessFulView.okGotItButton?.setOnClickListener(this@LinkStoreCardFragment)
            okGotItStaffButton?.setOnClickListener(this@LinkStoreCardFragment)
            uniqueIdsForLinkStoreCard()
        }
    }

    private fun LinkStoreCardProcessFragmentBinding.uniqueIdsForLinkStoreCard() {
        activity?.resources?.apply {
            incProcessingTextLayout?.root?.contentDescription =
                    getString(R.string.process_your_request_text_indicator)
            closeIconImageView?.contentDescription = getString(R.string.close_icon_tapped)
            ibBack?.contentDescription = getString(R.string.back_button_tapped)
            incLinkCardSuccessFulView?.root?.contentDescription = getString(R.string.card_success_layout)
            incLinkCardSuccessFulView.successTitleTextView?.contentDescription = getString(R.string.success_link_card_title)
            incLinkCardSuccessFulView.successLinkCardDescriptionTextView?.contentDescription =
                    getString(R.string.success_link_card_description)
            incLinkCardSuccessFulView.okGotItButton?.contentDescription = getString(R.string.success_got_it_button_tapped)
            incLinkCardFailure?.root?.contentDescription = getString(R.string.link_card_failure_layout)
            incLinkCardFailure.failureTitleTextView?.contentDescription =
                    getString(R.string.link_card_failure_title_label)
            incLinkCardFailure.failureLinkCardDescriptionTextView?.contentDescription =
                    getString(R.string.link_card_failure_description)
            incLinkCardFailure.btnRetryOnFailure?.contentDescription = getString(R.string.retry_on_failure_button)
            incLinkCardFailure.tvCallCenterNumber?.contentDescription = getString(R.string.call_center_number_tapped)
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
                        binding.linkStoreCardProgress()
                    }

                    override fun onSuccessHandler(response: LinkNewCardResponse) {
                        super.onSuccessHandler(response)
                        if (!isAdded) return

                        val account = Account()
                        account.accountNumber = storeDetails?.visionAccountNumber
                        account.productOfferingId = storeDetails?.productOfferingId?.toInt() ?: 0
                        when (mLinkCardType) {
                            LinkCardType.LINK_NEW_CARD.type -> {
                                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_LINK_CONFIRM, mCurrentActivity)
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
                                                binding.linkStoreCardSuccess()
                                            }

                                            LinkCardType.VIRTUAL_TEMP_CARD.type -> {
                                                binding.virtualStoreCardSuccess()
                                                handleStoreCardResponse(response)
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
                    override fun onFailureHandler(response: ServerErrorResponse?) {
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

    private fun LinkStoreCardProcessFragmentBinding.virtualStoreCardSuccess() {
        progressState()?.animateSuccessEnd(true)
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = VISIBLE
        incProcessingTextLayout?.root?.visibility = GONE
        includeVirtualTempCardSuccessMessage?.root?.visibility = VISIBLE
    }

    private fun onFailure() {
        if (!isAdded) return
        progressState()?.animateSuccessEnd(false)
        object : CountDownTimer(1500, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                binding.linkStoreCardFailure()
            }
        }.start()
    }

    private fun LinkStoreCardProcessFragmentBinding.linkStoreCardSuccess() {
        progressState()?.animateSuccessEnd(true)
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = GONE
        incLinkCardSuccessFulView?.root?.visibility = VISIBLE
        incProcessingTextLayout?.root?.visibility = GONE
    }

    private fun LinkStoreCardProcessFragmentBinding.linkStoreCardProgress() {
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = GONE
        incLinkCardSuccessFulView?.root?.visibility = GONE
        incProcessingTextLayout?.root?.visibility = VISIBLE
    }

    private fun LinkStoreCardProcessFragmentBinding.linkStoreCardFailure() {
        ibBack?.visibility = GONE
        closeIconImageView?.visibility = VISIBLE
        incLinkCardFailure?.root?.visibility = VISIBLE
        incProcessingTextLayout?.root?.visibility = GONE
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.tvCallCenterNumber -> Utils.makeCall( "0861 50 20 20")

                R.id.btnRetryOnFailure -> onAPIFailureRetry()

                R.id.ibBack -> onBackPressed()
                R.id.okGotItButton -> {
                    requireActivity().setResult(Activity.RESULT_OK)
                    this.finish()
                }
                R.id.closeIconImageView -> {
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                }
                R.id.okGotItStaffButton -> {
                    requireActivity().setResult(Activity.RESULT_OK)
                    this.finish()
                }
            }
        }
    }

    private fun refreshProductLandingPage(storeCardsResponse: StoreCardsResponse) {
        activity?.apply {
            val intent = Intent()
            intent.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
            this.setResult(ACTIVATE_VIRTUAL_TEMP_CARD_RESULT_CODE,intent)
            this.finish()
            this.overridePendingTransition(0, 0)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.supportFragmentManager?.apply {
            if (findFragmentById(R.id.flProgressIndicator) != null) {
                findFragmentById(R.id.flProgressIndicator)?.let { beginTransaction().remove(it).commitAllowingStateLoss() }
            }
        }
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun handleStoreCardResponse(storeCardsResponse: StoreCardsResponse) {
        activity?.let { activity ->
            val storeCardData = storeCardsResponse.storeCardsData

            val tempStoreCardData =
                    (activity as? MyCardActivityExtension)?.getStoreCardDetail()?.storeCardsData
            val tempProductOfferingId = tempStoreCardData?.productOfferingId
            val tempVisionAccountNumber = tempStoreCardData?.visionAccountNumber

            storeCardData?.visionAccountNumber = tempVisionAccountNumber ?: ""
            storeCardData?.productOfferingId = tempProductOfferingId ?: ""

            storeCardData?.apply {
                if(generateVirtualCard){
                    Handler().postDelayed({
                        val intent = Intent(activity, GetTemporaryStoreCardPopupActivity::class.java)
                        intent.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeCardsResponse))
                        activity.startActivity(intent)
                        activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        activity.finish()
                        activity.overridePendingTransition(0, 0)
                    }, AppConstant.DELAY_3000_MS)
                }
                else {
                    if(isStaffMember
                        && virtualCardStaffMemberMessage?.title != null
                        && virtualCardStaffMemberMessage.paragraphs.isNotEmpty()
                    ){//show staff discount congratulations view
                        binding.flProgressIndicator.visibility = GONE
                        binding.includeVirtualTempCardSuccessMessage?.root?.visibility = GONE
                        binding.includeVirtualTempCardSuccessStaffMessage?.root?.visibility = VISIBLE
                        titleStaffTextView?.text = virtualCardStaffMemberMessage.title
                        if(virtualCardStaffMemberMessage.paragraphs.size >= 3){
                            staffMessage1CheckBox.text = virtualCardStaffMemberMessage.paragraphs[0]
                            staffMessage2CheckBox.text = virtualCardStaffMemberMessage.paragraphs[1]
                            staffMessage3CheckBox.text = virtualCardStaffMemberMessage.paragraphs[2]
                        }
                    }
                    else{
                        viewLifecycleOwner.lifecycleScope.launch {
                            delay(AppConstant.DELAY_3000_MS)
                            refreshProductLandingPage(storeCardsResponse)
                        }
                    }
                }
            }
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

    fun showLoader() {
        (activity as? AppCompatActivity)?.addFragment(
            fragment = ProgressStateFragment.newInstance(this),
            tag = ProgressStateFragment::class.java.simpleName,
            containerViewId = R.id.flProgressIndicator
        )
    }

    fun progressState(): ProgressStateFragment? = (activity as? AppCompatActivity)?.findFragmentByTag(ProgressStateFragment::class.java.simpleName) as? ProgressStateFragment

    override fun onAnimationEnd(isAnimationFinished: Boolean) {}
}