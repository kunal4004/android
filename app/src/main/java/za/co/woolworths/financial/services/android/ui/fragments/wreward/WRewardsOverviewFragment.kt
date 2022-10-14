package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.animation.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
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
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.adapters.FeaturedPromotionsAdapter
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.Utils.triggerFireBaseEvents

import androidx.annotation.RequiresApi
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.CountDownTimerImpl
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.ScreenBrightnessDelegate
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.ScreenBrightnessImpl
import androidx.activity.result.contract.ActivityResultContracts
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.ScreenBrightnessImpl.Companion.HUNDRED_PERCENT_VALUE
import android.animation.AnimatorListenerAdapter
import kotlinx.android.synthetic.main.wrewards_virtual_card_number_row.*
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.ShakeDetectorImpl
import za.co.woolworths.financial.services.android.ui.fragments.wreward.logged_in.WRewardsLoggedinAndLinkedFragment

class WRewardsOverviewFragment : Fragment(), View.OnClickListener {

    private var initialBrightness: Int = 0
    private var mScreenBrightnessDelegate: ScreenBrightnessDelegate? = null
    private val TAGREWARD: String = WRewardsOverviewFragment::class.java.simpleName
    private var mIsBackVisible: Boolean = false
    private var mSetLeftIn: AnimatorSet? = null
    private var mSetRightOut: AnimatorSet? = null
    private var currentStatus: String? = null
    private var cardDetailsResponse: CardDetailsResponse? = null
    private var bundle: Bundle? = null
    private var voucherResponse: VoucherResponse? = null
    private var mErrorHandlerView: ErrorHandlerView? = null
    private var shortAnimationDuration: Int = 1700

    // variable to track event time
    private var mLastClickTime: Long = 0
    private var tireStatusVIP = "vip"
    private lateinit var requestWriteSettingsFromFragment : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments
        voucherResponse = Gson().fromJson(bundle?.getString("WREWARDS"), VoucherResponse::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.apply { triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSOVERVIEW, this) }
        return inflater.inflate(R.layout.wrewards_overview_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBrightnessControl()

        activity?.let { activity ->
            mErrorHandlerView = ErrorHandlerView(activity, no_connection_layout)
            mErrorHandlerView?.setMargin(no_connection_layout, 0, 0, 0, 0)
        }

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
        moreInfoVirtualCardTextView.setOnClickListener(this)
        rightIndicatorIconImageView.setOnClickListener(this)
        flipCardFrontLayout.setOnClickListener(this)
        shakeOrTapNumberTextView.setOnClickListener(this)

        uniqueIdsForWRewardOverview()
    }

    private fun initBrightnessControl() {
        mScreenBrightnessDelegate = ScreenBrightnessDelegate(
            ScreenBrightnessImpl(),
            CountDownTimerImpl(),
            ShakeDetectorImpl(this)
        )

        mScreenBrightnessDelegate?.apply {

            setBrightnessLevel()

            initBrightnessChangeListener {
                if (it < 255)
                    setBrightnessLevel()
            }

            registerLifeCycle(lifecycle)
            shakeDetectorInit {
                val isCurrentFragmentWRewardsFragmentSection =
                    (activity as? BottomNavigationActivity)?.currentFragment is WRewardsFragment
                val isCurrentFragmentWRewardsOverviewFragment =
                    (parentFragment as? WRewardsLoggedinAndLinkedFragment)?.wrewardsViewPager?.currentItem == 0
                // disable shake action when barcode is invisible
                if (SessionUtilities.getInstance().isUserAuthenticated &&
                    SessionUtilities.getInstance().isC2User &&
                    (barCodeNumber?.text?.length ?: 0 > 0) &&
                    isCurrentFragmentWRewardsFragmentSection &&
                    isCurrentFragmentWRewardsOverviewFragment
                ) {
                    setShakeToAnimateView(activity, flipCardBackLayout)
                    shakeOrTapToBrightness()
                }
            }

            // Custom activity result contract
            requestWriteSettingsFromFragment =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    // parseResult will return this as string?
                    if (isSettingPermissionAllowedForOneApp()) {
                        controlBrightness()
                    }
                }
        }
    }

    private fun ScreenBrightnessDelegate.setBrightnessLevel() {
        initialBrightness = convertBrightnessLevelToPercent(
            mScreenBrightnessDelegate?.getScreenBrightness() ?: 0
        )
    }

    private fun uniqueIdsForWRewardOverview() {
        activity?.resources?.apply {
//            wRewardCardFrameLayout?.contentDescription = getString(R.string.wreward_flip_card_framelayout)
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

    fun scrollToTop() = scrollWRewardsOverview?.let { ObjectAnimator.ofInt(it, "scrollY", it.scrollY, 0).setDuration(500).start() }

    private fun handleTireHistoryView(tireInfo: TierInfo) {
        overviewLayout.visibility = VISIBLE
        noTireHistory.visibility = GONE
        currentStatus = tireInfo.currentTier
        savings.setText(CurrencyFormatter.formatAmountToRandAndCentWithSpace(tireInfo.earned))
        flipCardFrontLayout.setOnClickListener(this)
        flipCardBackLayout.setOnClickListener(this)
        currentStatus?.let {
            if (!it.contains(tireStatusVIP, true)) {
                toNextTireLayout.visibility = VISIBLE
                vipLogo.visibility = GONE
                toNextTire.setText(CurrencyFormatter.formatAmountToRandAndCentWithSpace(tireInfo.toSpend))
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
                override fun onAnimationEnd(animation: Animator) {
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
        activity?.apply { triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSFLIP, this) }
        mIsBackVisible = if (!mIsBackVisible) {
            mSetRightOut?.setTarget(flipCardFrontLayout)
            mSetLeftIn?.setTarget(flipCardBackLayout)
            mSetRightOut?.start()
            mSetLeftIn?.start()
            if (barCodeNumber?.text?.isEmpty() !=  true) {
                shakeOrTapNumberTextView?.apply {
                    // Set the content view to 0% opacity but visible, so that it is visible
                    // (but fully transparent) during the animation.
                    alpha = 0f
                    visibility = VISIBLE

                    // Animate the content view to 100% opacity, and clear any animation
                    // listener set on the view.
                    animate()
                            .alpha(1f)
                            .setDuration(shortAnimationDuration.toLong())
                            .setListener(null)
                }
            }
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

    @RequiresApi(Build.VERSION_CODES.M)
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

            R.id.moreInfoVirtualCardTextView,R.id.rightIndicatorIconImageView -> {
                activity?.supportFragmentManager?.let { VirtualCardNumberInfoDialogFragment.newInstance().show(it, VirtualCardNumberInfoDialogFragment::class.java.simpleName) }
            }

            R.id.shakeOrTapNumberTextView ->  shakeOrTapToBrightness()

            else -> return
        }
    }

    private fun shakeOrTapToBrightness() {
        mScreenBrightnessDelegate?.apply {
            if (!isSettingPermissionAllowedForOneApp()) {
                requestWriteSettingsFromFragment.launch(
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:" + activity?.packageName)
                    }
                )
            }else {
                controlBrightness()
            }
        }
    }

    private fun ScreenBrightnessDelegate.controlBrightness() {
        setBrightnessModeManual()
        setScreenBrightness(HUNDRED_PERCENT_VALUE)
        startTimer { setScreenBrightness(initialBrightness) }
    }

    private fun handleNoTireHistoryView() {
        overviewLayout?.visibility = GONE
        noTireHistory?.visibility = VISIBLE
    }

    private fun loadPromotionsAPI() {
        mErrorHandlerView?.hideErrorHandlerLayout()
        val promotionsResponseCall = OneAppService.getPromotions()
        promotionsResponseCall.enqueue(CompletionHandler(object : IResponseListener<PromotionsResponse> {
            override fun onSuccess(response: PromotionsResponse?) {
                response?.let { handlePromotionResponse(it) }
            }

            override fun onFailure(error: Throwable?) {
                error?.message?.let {  mErrorHandlerView?.networkFailureHandler(it)}
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
        hideBackButtonAndToolbarBorder()
        mScreenBrightnessDelegate?.registerContentObserverForBrightness()
        activity?.apply { Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_OVERVIEW) }
    }

    override fun onDestroy() {
        super.onDestroy()
        mScreenBrightnessDelegate?.unregisterContentObserverForBrightness()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            hideBackButtonAndToolbarBorder()
        }
    }

    private fun hideBackButtonAndToolbarBorder() {
        (activity as? BottomNavigationActivity)?.apply {
            if (currentFragment is WRewardsFragment) {
                showBackNavigationIcon(false)
                setToolbarBackgroundColor(R.color.white)
            }
        }
    }
}