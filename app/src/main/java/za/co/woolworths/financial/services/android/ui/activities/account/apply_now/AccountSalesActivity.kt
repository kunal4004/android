package za.co.woolworths.financial.services.android.ui.activities.account.apply_now

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.account_sales_activity.*
import kotlinx.android.synthetic.main.account_sales_card_header.*
import kotlinx.android.synthetic.main.account_sign_out_activity.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import za.co.woolworths.financial.services.android.contracts.IAccountSalesContract
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardType
import za.co.woolworths.financial.services.android.ui.views.SetUpViewPagerWithTab
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class AccountSalesActivity : AppCompatActivity(), IAccountSalesContract.AccountSalesView, OnClickListener, (Int) -> Unit, (View, Int) -> Unit {

    private var mAccountSalesModelImpl: AccountSalesPresenterImpl? = null
    private var sheetBehavior: BottomSheetBehavior<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_sales_activity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        KotlinUtils.setTransparentStatusBar(this)

        mAccountSalesModelImpl = AccountSalesPresenterImpl(this, AccountSalesModelImpl())
        mAccountSalesModelImpl?.apply {
            setAccountSalesIntent(intent)
            switchAccountSalesProduct()
        }
        setupToolbarTopMargin()
        setupBottomSheetBehaviour()

        storeCardApplyNowButton?.setOnClickListener(this)
        bottomApplyNowButton?.setOnClickListener(this)
        navigateBackImageButton?.setOnClickListener(this)

        AnimationUtilExtension.animateViewPushDown(storeCardApplyNowButton)
        AnimationUtilExtension.animateViewPushDown(bottomApplyNowButton)
        AnimationUtilExtension.animateViewPushDown(navigateBackImageButton)
        AnimationUtilExtension.animateViewPushDown(cardFrontImageView)
        AnimationUtilExtension.animateViewPushDown(cardBackImageView)
    }

    private fun setupToolbarTopMargin() {
        val bar = findViewById<Toolbar>(R.id.toolbar)
        val params = bar?.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight(this)
        bar?.layoutParams = params
    }

    private fun setupBottomSheetBehaviour() {
        val bottomSheetLayout = findViewById<LinearLayout>(R.id.incBottomSheetLayout)
        sheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheetLayout)

        val anchoredHeight = mAccountSalesModelImpl?.getAnchoredHeight(0f, toolbar) ?: 0
        incBottomSheetLayout?.setPadding(0, anchoredHeight, 0, 0)

        val overlayAnchoredHeight = mAccountSalesModelImpl?.getOverlayAnchoredHeight() ?: 0
        sheetBehavior?.peekHeight = overlayAnchoredHeight
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                invoke(bottomSheet, newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                transitionBottomSheetBackgroundColor(slideOffset)
                navigateBackImageButton?.rotation = slideOffset * -90
            }
        })

    }

    override fun displayAccountSalesBlackInfo(storeCard: AccountSales) {
        mAccountSalesModelImpl?.setAccountSalesDetailPage(storeCard, findNavController(R.id.nav_host_fragment))
    }

    override fun displayCreditCardFrontUI(position: Int) {
        blackAndGoldCreditCardViewPager?.currentItem = position
    }

    override fun displayHeaderItems(cardHeader: CardHeader?) {
        cardHeader?.apply {
            storeCardTitleTextView?.text = title
            storeCardDescriptionTextView?.text = description
            incAccountSalesFrontLayout?.setBackgroundResource(drawables[0])
            cardFrontImageView?.setImageResource(drawables[1])
            cardBackImageView?.setImageResource(drawables[2])
        }
    }

    override fun displayCreditCard(fragmentList: Map<String, Fragment>?, position: Int) {
        nav_host_fragment?.view?.visibility = GONE
        blackAndGoldCreditCardViewPager?.visibility = VISIBLE
        SetUpViewPagerWithTab(this, blackAndGoldCreditCardViewPager, tabLayout, fragmentList, position, this).create()
        invoke(position)
    }

    override fun onBackPressed() {
        // Collapse overlay view if view is opened, else navigate to previous screen
        if (sheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            return
        }

        mAccountSalesModelImpl?.onBackPressed(this@AccountSalesActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccountSalesModelImpl?.onDestroy()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.storeCardApplyNowButton, R.id.bottomApplyNowButton -> mAccountSalesModelImpl?.onApplyNowButtonTapped()
            R.id.navigateBackImageButton -> onBackPressed()
        }
    }

    override fun invoke(position: Int) {
        when (position) {
            CreditCardType.GOLD_CREDIT_CARD.ordinal -> {
                val goldCreditCard = mAccountSalesModelImpl?.getCreditCard()?.get(position)
                displayHeaderItems(goldCreditCard?.cardHeader)
                cardFrontImageView?.visibility = VISIBLE
                cardFrontBlackImageView?.visibility = GONE
                goldCreditCard?.cardHeader?.drawables?.get(1)?.let { drawable -> cardFrontImageView?.setImageResource(drawable) }
            }
            CreditCardType.BLACK_CREDIT_CARD.ordinal -> {
                val blackCreditCard = mAccountSalesModelImpl?.getCreditCard()?.get(position)
                displayHeaderItems(blackCreditCard?.cardHeader)
                cardFrontImageView?.visibility = INVISIBLE
                cardFrontBlackImageView?.visibility = VISIBLE
                blackCreditCard?.cardHeader?.drawables?.get(1)?.let { drawable -> cardFrontBlackImageView?.setImageResource(drawable) }
            }
            else -> throw RuntimeException("Invalid View Pager Page Selected ")
        }
    }

    override fun invoke(view: View, position: Int) {
        when (position) {
            BottomSheetBehavior.STATE_COLLAPSED -> animateButtonOut()
            BottomSheetBehavior.STATE_EXPANDED -> animateButtonIn()
        }
    }

    private fun animateButtonOut() {
        val animate = TranslateAnimation(0f, 0f, 0f, bottomApplyNowButtonRelativeLayout.height.toFloat())
        animate.duration = 500
        animate.fillAfter = true
        bottomApplyNowButtonRelativeLayout?.startAnimation(animate)
        bottomApplyNowButtonRelativeLayout?.visibility = INVISIBLE
        bottomApplyNowButtonRelativeLayout?.isEnabled = false
    }

    private fun animateButtonIn() {
        bottomApplyNowButtonRelativeLayout?.visibility = VISIBLE
        val animate = TranslateAnimation(0f, 0F, bottomApplyNowButtonRelativeLayout.height.toFloat(), 0f)
        animate.duration = 500
        animate.fillAfter = true
        bottomApplyNowButtonRelativeLayout?.startAnimation(animate)
        bottomApplyNowButtonRelativeLayout?.isEnabled = true
    }

    private fun transitionBottomSheetBackgroundColor(slideOffset: Float) {
        val colorFrom = ContextCompat.getColor(this, android.R.color.transparent)
        val colorTo = ContextCompat.getColor(this, R.color.black_99)
        dimView?.setBackgroundColor(KotlinUtils.interpolateColor(slideOffset, colorFrom, colorTo))
    }
}