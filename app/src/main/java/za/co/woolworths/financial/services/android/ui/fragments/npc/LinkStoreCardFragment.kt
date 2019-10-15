package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.link_store_card_process_fragment.*
import kotlinx.android.synthetic.main.npc_card_linked_successful_layout.*
import kotlinx.android.synthetic.main.npc_link_store_card_failure.*
import kotlinx.android.synthetic.main.process_block_card_fragment.incLinkCardSuccessFulView
import kotlinx.android.synthetic.main.process_block_card_fragment.incProcessingTextLayout
import za.co.woolworths.financial.services.android.contracts.IOTPLinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.LinkStoreCard
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsData
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.InstantStoreCardReplacementActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.GetTemporaryStoreCardPopupActivity
import za.co.woolworths.financial.services.android.ui.fragments.WStoreCardFragment.REQUEST_CODE_BLOCK_MY_STORE_CARD
import za.co.woolworths.financial.services.android.util.Utils


class LinkStoreCardFragment : AnimatedProgressBarFragment(), View.OnClickListener {

    private var mStoreCardsResponse: StoreCardsResponse? = null
    private var storeDetails: StoreCardsData? = null
    private var otpMethodType: OTPMethodType? = null
    private var linkStoreCard: LinkStoreCard? = null

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
        ibClose?.setOnClickListener(this)
        ibBack?.setOnClickListener(this)
        okGotItButton?.setOnClickListener(this)
    }

    private fun linkStoreCardRequest() {
        (activity as? InstantStoreCardReplacementActivity)?.apply {
            otpMethodType = getOTPMethodType()
            storeDetails = getStoreCardDetail().storeCardsData
            linkStoreCard = LinkStoreCard(storeDetails?.productOfferingId?.toInt()
                    ?: 0, storeDetails?.visionAccountNumber
                    ?: "", getCardNumber(), getSequenceNumber(), getOtpNumber(), getOTPMethodType())
            linkStoreCard?.let { request ->
                val storeCardOTPRequest = otpMethodType?.let { otp -> StoreCardOTPRequest(this, otp) }
                storeCardOTPRequest?.make(object : IOTPLinkStoreCard<LinkNewCardResponse> {
                    override fun startLoading() {
                        super.startLoading()
                        linkStoreCardProgress()
                    }

                    override fun onSuccessHandler(response: LinkNewCardResponse) {
                        super.onSuccessHandler(response)
                        if (!isAdded) return

                        val account = Account()
                        account.accountNumber = storeDetails?.visionAccountNumber
                        account.productOfferingId = storeDetails?.productOfferingId?.toInt() ?: 0

                        // Make store card call
                        storeCardOTPRequest.getStoreCards(object : IOTPLinkStoreCard<StoreCardsResponse> {

                            override fun onSuccessHandler(response: StoreCardsResponse) {
                                if (!isAdded) return
                                super.onSuccessHandler(response)
                                mStoreCardsResponse = response
                                (activity as? InstantStoreCardReplacementActivity)?.clearFlag()
                                progressState()?.animateSuccessEnd(true)
                                object : CountDownTimer(1500, 100) {
                                    override fun onTick(millisUntilFinished: Long) {
                                    }

                                    override fun onFinish() {
                                        linkStoreCardSuccess()
                                    }
                                }.start()
                            }

                            override fun onFailureHandler() {
                                super.onFailureHandler()
                                onFailure()
                            }
                        }, account)
                    }

                    override fun onFailureHandler() {
                        super.onFailureHandler()
                        onFailure()
                    }
                }, request)
            }
        }
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
        ibClose?.visibility = VISIBLE
        incLinkCardSuccessFulView?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    private fun linkStoreCardProgress() {
        ibBack?.visibility = GONE
        ibClose?.visibility = GONE
        incLinkCardSuccessFulView?.visibility = GONE
        incProcessingTextLayout?.visibility = VISIBLE
    }

    private fun linkStoreCardFailure() {
        ibBack?.visibility = GONE
        ibClose?.visibility = VISIBLE
        incLinkCardFailure?.visibility = VISIBLE
        incProcessingTextLayout?.visibility = GONE
    }

    override fun onClick(view: View?) {
        activity?.apply {
            when (view?.id) {
                R.id.tvCallCenterNumber -> Utils.makeCall(this, "0861 50 20 20")

                R.id.btnRetryOnFailure -> {
                    val storeCardResponse = (this as? InstantStoreCardReplacementActivity)?.getStoreCardDetail()
                    (this as? AppCompatActivity)?.let { activity -> navigateToLinkNewCardActivity(activity, Gson().toJson(storeCardResponse)) }
                }
                R.id.ibBack -> onBackPressed()
                R.id.okGotItButton -> {
                    mStoreCardsResponse?.let { handleStoreCardResponse(it) }
                }
                R.id.ibClose -> {
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

            storeCardsResponse.storeCardsData?.apply {
                if (generateVirtualCard) {
                    activity.startActivity(Intent(activity, GetTemporaryStoreCardPopupActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                } else {
                    val displayStoreCardDetail = Intent(activity, MyCardDetailActivity::class.java)
                    displayStoreCardDetail.putExtra(STORE_CARD_DETAIL, Gson().toJson(storeDetails))
                    activity.startActivityForResult(displayStoreCardDetail, REQUEST_CODE_BLOCK_MY_STORE_CARD)
                    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                }
            }

            activity.finish()
            activity.overridePendingTransition(0, 0)
        }

    }
}