package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import kotlinx.android.synthetic.main.no_connection_handler.*
import kotlinx.android.synthetic.main.wrewards_overview_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.CardDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.PromotionsResponse
import za.co.woolworths.financial.services.android.models.dto.TierInfo
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.WRewardBenefitActivity
import za.co.woolworths.financial.services.android.ui.adapters.FeaturedPromotionsAdapter
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.Utils.triggerFireBaseEvents

class WRewardsOverviewFragment : Fragment(), View.OnClickListener {

    private val TAGREWARD: String? = WRewardsOverviewFragment::class.java.simpleName
    private var mIsBackVisible: Boolean = false
    private var mSetLeftIn: AnimatorSet? = null
    private var mSetRightOut: AnimatorSet? = null
    private var currentStatus: String? = null
    private var cardDetailsResponse: CardDetailsResponse? = null
    private var bundle: Bundle? = null
    private var voucherResponse: VoucherResponse? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    // variable to track event time
    private var mLastClickTime: Long = 0
    private var tireStatusVIP = "vip"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments
        voucherResponse = Gson().fromJson(bundle?.getString("WREWARDS"), VoucherResponse::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSOVERVIEW)
        return inflater.inflate(R.layout.wrewards_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { activity ->
            mErrorHandlerView = ErrorHandlerView(activity, no_connection_layout)
            mErrorHandlerView?.setMargin(no_connection_layout, 0, 0, 0, 0)
        }

        loadDefaultCardType()

        voucherResponse?.tierInfo?.apply {
            handleTireHistoryView(this)
            bundle?.apply {
                if (containsKey("CARD_DETAILS")) {
                    cardDetailsResponse = Gson().fromJson(getString("CARD_DETAILS"), CardDetailsResponse::class.java)
                    handleCard(cardDetailsResponse)
                } else {
                    handleNoTireHistoryView()
                }
            }
        }

        infoImage.setOnClickListener(this)
        tvMoreInfo.setOnClickListener(this)
        btnRetry.setOnClickListener(this)
        tvMoreInfoVirtualCard.setOnClickListener(this)

        uniqueIdsForWRewardOverview()
    }

    private fun uniqueIdsForWRewardOverview() {
        activity?.resources?.apply {
            wRewardCardFrameLayout?.contentDescription = getString(R.string.wreward_flip_card_framelayout)
            flipCardBackLayout?.contentDescription = getString(R.string.flipCardBackLayout)
            cardBackgroundLinearLayout?.contentDescription = getString(R.string.cardBackgroundLinearLayout)
            cardFrameLayout?.contentDescription = getString(R.string.card_frame_layout)
            wRewardsBenefitsRelativeLayout?.contentDescription = getString(R.string.tvBenefitRewardLayout)
            wRewardSavingsRelativeLayout?.contentDescription = getString(R.string.savingsLayout)
            toNextTireLayout?.contentDescription = getString(R.string.toNextTireLayout)
            featurePromotionTetView?.contentDescription = getString(R.string.featured_promotions_label)
            promotionsLayout?.contentDescription = getString(R.string.featured_promotions)
            vipLogo?.contentDescription = getString(R.string.vipLogoLayout)
        }
    }

    private fun loadDefaultCardType() {
        flipCardFrontLayout?.setBackgroundResource(R.drawable.wrewards_card)
        flipCardBackLayout?.setBackgroundResource(R.drawable.wrewards_card_flipped)
    }

    fun scrollToTop() = scrollWRewardsOverview?.let { ObjectAnimator.ofInt(it, "scrollY", it.scrollY, 0).setDuration(500).start() }

    private fun handleTireHistoryView(tireInfo: TierInfo) {
        overviewLayout.visibility = VISIBLE
        noTireHistory.visibility = GONE
        currentStatus = tireInfo.currentTier
        savings.setText(WFormatter.formatAmount(tireInfo.earned))
        flipCardFrontLayout.setOnClickListener(this)
        flipCardBackLayout.setOnClickListener(this)
        currentStatus?.let {
            if (!it.contains(tireStatusVIP, true)) {
                toNextTireLayout.visibility = VISIBLE
                vipLogo.visibility = GONE
                toNextTire.setText(WFormatter.formatAmount(tireInfo.toSpend))
            }
        }
        loadPromotionsAPI()
    }

    private fun handleCard(cardDetailsResponse: CardDetailsResponse?) {
        if (activity == null) return
        cardDetailsResponse?.apply {
            if (cardType != null && cardNumber != null) {
                when {
                    cardDetailsResponse.cardType.equals(CardType.WREWARDS.type, ignoreCase = true) -> {
                        flipCardFrontLayout.setBackgroundResource(R.drawable.wrewards_card)
                        flipCardBackLayout.setBackgroundResource(R.drawable.wrewards_card_flipped)
                        if (mIsBackVisible)
                            showVIPLogo()
                    }
                    cardType.equals(CardType.MYSCHOOL.type, ignoreCase = true) -> {
                        flipCardFrontLayout.setBackgroundResource(R.drawable.myschool_card)
                        flipCardBackLayout.setBackgroundResource(R.drawable.myschool_card_flipped)
                    }
                    else -> return
                }
                barCodeNumber.setText(WFormatter.formatVoucher(cardNumber))
                try {
                    barCodeImage.setImageBitmap(Utils.encodeAsBitmap(cardNumber, BarcodeFormat.CODE_128, barCodeImage.width, 60))
                } catch (e: WriterException) {
                    Log.d(TAGREWARD, e.message ?: "")
                }
                loadAnimations()
                changeCameraDistance()
                val handler = Handler()
                handler.postDelayed({ flipCard() }, 1000)
            } else {
                showVIPLogo()
            }
        }
    }

    private fun loadAnimations() {
        activity?.apply {

            mSetRightOut = AnimatorInflater.loadAnimator(this, R.animator.card_flip_out) as? AnimatorSet
            mSetLeftIn = AnimatorInflater.loadAnimator(this, R.animator.card_flip_in) as? AnimatorSet

            mSetLeftIn?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    showVIPLogo()
                }
            })

        }
    }

    private fun showVIPLogo() {
            currentStatus?.let { state ->
                if (state.contains(tireStatusVIP, ignoreCase = true)) {
                    vipLogo?.visibility = VISIBLE
                }
            }
    }

    private fun flipCard() {
        triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSFLIP)
        mIsBackVisible = if (!mIsBackVisible) {
            mSetRightOut?.setTarget(flipCardFrontLayout)
            mSetLeftIn?.setTarget(flipCardBackLayout)
            mSetRightOut?.start()
            mSetLeftIn?.start()
            true
        } else {
            mSetRightOut?.setTarget(flipCardBackLayout)
            mSetLeftIn?.setTarget(flipCardFrontLayout)
            mSetRightOut?.start()
            mSetLeftIn?.start()
            false
        }
    }

    private fun changeCameraDistance() {
        val distance = 8000
        val scale = resources.displayMetrics.density * distance
        flipCardFrontLayout?.cameraDistance = scale
        flipCardBackLayout?.cameraDistance = scale
    }

    override fun onClick(view: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view?.id) {
            R.id.infoImage, R.id.tvMoreInfo -> {
                activity?.apply {

                    currentStatus?.let {
                        val intent = Intent(this, WRewardBenefitActivity::class.java)
                        intent.putExtra("benefitTabPosition", if (it.contains(tireStatusVIP, true)) 1 else 0)
                        startActivity(intent)
                        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                    }
                }
            }

            R.id.btnRetry -> {
                activity?.apply {
                    if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
                        loadPromotionsAPI()
                    }
                }
            }
            R.id.tvMoreInfoVirtualCard -> {
                activity?.supportFragmentManager?.let { VirtualCardNumberInfoDialogFragment.newInstance().show(it, VirtualCardNumberInfoDialogFragment::class.java.simpleName) }
            }
            else -> return
        }
    }

    private fun handleNoTireHistoryView() {
        overviewLayout?.visibility = GONE
        noTireHistory?.visibility = VISIBLE
    }

    private fun loadPromotionsAPI() {
        mErrorHandlerView?.hideErrorHandlerLayout()
        val promotionsResponseCall = OneAppService.getPromotions()
        promotionsResponseCall.enqueue(CompletionHandler(object : IResponseListener<PromotionsResponse> {
            override fun onSuccess(promotionsResponse: PromotionsResponse) {
                handlePromotionResponse(promotionsResponse)
            }

            override fun onFailure(error: Throwable) {
                if (error.message == null) return
                mErrorHandlerView?.networkFailureHandler(error.message)
            }
        }, PromotionsResponse::class.java))

    }

    fun handlePromotionResponse(promotionsResponse: PromotionsResponse) {
        try {
            with(promotionsResponse) {
                if (httpCode == 200) {
                    if (promotions.size > 0) {
                        promotionViewPager?.adapter = activity?.let { FeaturedPromotionsAdapter(it, promotions) }
                    }
                }
            }
        } catch (npe: NullPointerException) {
            Log.d(TAGREWARD, npe.message ?: "")
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.apply { Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_OVERVIEW) }
    }
}