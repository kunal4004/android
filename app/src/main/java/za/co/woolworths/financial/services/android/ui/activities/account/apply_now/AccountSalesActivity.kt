package za.co.woolworths.financial.services.android.ui.activities.account.apply_now

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2.*
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountSalesActivityBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import za.co.woolworths.financial.services.android.contracts.IAccountSalesContract
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardType
import za.co.woolworths.financial.services.android.ui.extension.findFragmentAtPosition
import za.co.woolworths.financial.services.android.ui.extension.underline
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragmentViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.apply_now.AccountSalesFragment
import za.co.woolworths.financial.services.android.ui.views.ConfigureViewPagerWithTab
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class AccountSalesActivity : AppCompatActivity(), IAccountSalesContract.AccountSalesView, OnClickListener, (Int) -> Unit {

    private lateinit var binding: AccountSalesActivityBinding
    private var sheetBehavior: BottomSheetBehavior<*>? = null
    var mAccountSalesModelImpl: AccountSalesPresenterImpl? = null
    var mBottomSheetBehaviorState: Int = BottomSheetBehavior.STATE_COLLAPSED

    private val myAccountsFragmentViewModel: MyAccountsFragmentViewModel by viewModels()

    @SuppressLint("SourceLockedOrientationActivity", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AccountSalesActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        myAccountsFragmentViewModel.getAccountPresenter(null)
        KotlinUtils.setTransparentStatusBar(this)

        with(binding) {
            incAccountSalesFrontLayout.viewApplicationStatusTextView?.apply {
                underline()
                AnimationUtilExtension.animateViewPushDown(this)
                setOnClickListener(this@AccountSalesActivity)
            }
            mAccountSalesModelImpl = AccountSalesPresenterImpl(this@AccountSalesActivity, AccountSalesModelImpl())
            mAccountSalesModelImpl?.apply {
                setAccountSalesIntent(intent)
                switchAccountSalesProduct()
            }
            setupToolbarTopMargin()
            setupBottomSheetBehaviour()

            incAccountSalesFrontLayout.applyNowHeaderButton?.setOnClickListener(this@AccountSalesActivity)
            bottomApplyNowButton?.setOnClickListener(this@AccountSalesActivity)
            navigateBackImageButton?.setOnClickListener(this@AccountSalesActivity)

            AnimationUtilExtension.animateViewPushDown(incAccountSalesFrontLayout.applyNowHeaderButton)
            AnimationUtilExtension.animateViewPushDown(bottomApplyNowButton)
            AnimationUtilExtension.animateViewPushDown(navigateBackImageButton)
            AnimationUtilExtension.animateViewPushDown(incAccountSalesFrontLayout.accountSalesCardHeader.cardFrontImageView)
            AnimationUtilExtension.animateViewPushDown(incAccountSalesFrontLayout.accountSalesCardHeader.cardBackImageView)


            incBottomSheetLayout.blackAndGoldCreditCardViewPager?.registerOnPageChangeCallback(object :
                OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onTabStateChange(mBottomSheetBehaviorState)
                }
            })

            // Disable scrolling when activity starts for ViewPager
            onTabStateChange(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    private fun setupToolbarTopMargin() {
        val bar = findViewById<Toolbar>(R.id.toolbar)
        val params = bar?.layoutParams as? ViewGroup.MarginLayoutParams
        params?.topMargin = KotlinUtils.getStatusBarHeight()
        bar?.layoutParams = params
    }

    private fun setupBottomSheetBehaviour() {
        val bottomSheetBehaviourLinearLayout = findViewById<LinearLayout>(R.id.incBottomSheetLayout)
        val layoutParams = bottomSheetBehaviourLinearLayout?.layoutParams
        layoutParams?.height = mAccountSalesModelImpl?.bottomSheetBehaviourHeight()
        bottomSheetBehaviourLinearLayout?.requestLayout()
        sheetBehavior = BottomSheetBehavior.from(bottomSheetBehaviourLinearLayout)
        sheetBehavior?.peekHeight = mAccountSalesModelImpl?.bottomSheetPeekHeight() ?: 0
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                mBottomSheetBehaviorState = newState
                onTabStateChange(newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                with(binding) {
                    AnimationUtilExtension.transitionBottomSheetBackgroundColor(dimView, slideOffset)
                    navigateBackImageButton?.rotation = slideOffset * -90
                    if (slideOffset > 0.2)
                        AnimationUtilExtension.animateButtonIn(bottomApplyNowButtonRelativeLayout)
                    else
                        AnimationUtilExtension.animateButtonOut(bottomApplyNowButtonRelativeLayout)
                }
            }
        })
    }

    private fun onTabStateChange(newState: Int?) {
        with(binding.incBottomSheetLayout) {
            val currentFragment = blackAndGoldCreditCardViewPager?.currentItem?.let {
                getCurrentFragment(blackAndGoldCreditCardViewPager?.currentItem ?: 0)
            }
            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    currentFragment?.smoothScrollToTop()
                    currentFragment?.setScrollingEnabled(false)
                }

                BottomSheetBehavior.STATE_EXPANDED -> currentFragment?.setScrollingEnabled(true)
                else -> currentFragment?.setScrollingEnabled(false)
            }
        }
    }

    override fun displayAccountSalesBlackInfo(storeCard: AccountSales) {
        mAccountSalesModelImpl?.setAccountSalesDetailPage(storeCard, findNavController(R.id.nav_host_fragment))
    }

    override fun displayCreditCardFrontUI(position: Int) {
        binding.incBottomSheetLayout.blackAndGoldCreditCardViewPager?.currentItem = position
    }

    override fun displayHeaderItems(cardHeader: CardHeader?) {
        with(binding) {
            cardHeader?.apply {
                incAccountSalesFrontLayout.titleTextView?.text = title
                incAccountSalesFrontLayout.descriptionTextView?.text = description
                incAccountSalesFrontLayout?.root?.setBackgroundResource(drawables[0])
                incAccountSalesFrontLayout.accountSalesCardHeader.cardFrontImageView?.setImageResource(drawables[1])
                incAccountSalesFrontLayout.accountSalesCardHeader.cardBackImageView?.setImageResource(drawables[2])
            }
        }
    }

    override fun displayCreditCard(fragmentList: Map<String, Fragment>?, position: Int) {
        with(binding) {
            val navHost = supportFragmentManager.findFragmentById(R.id.replacementCardNavHost) as NavHostFragment?
            navHost?.view?.visibility = GONE
            incBottomSheetLayout.tabLinearLayout?.visibility = VISIBLE
            ConfigureViewPagerWithTab(
                this@AccountSalesActivity,
                incBottomSheetLayout.blackAndGoldCreditCardViewPager,
                incBottomSheetLayout.tabLayout,
                fragmentList,
                position,
                this@AccountSalesActivity
            ).create()
            invoke(position)
        }
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
            R.id.applyNowHeaderButton, R.id.bottomApplyNowButton -> {
                mAccountSalesModelImpl?.onApplyNowButtonTapped()?.let { url -> KotlinUtils.openUrlInPhoneBrowser(url, this) }
            }
            R.id.navigateBackImageButton -> onBackPressed()

            R.id.viewApplicationStatusTextView -> {
               val applyNowSection =  when(mAccountSalesModelImpl?.getApplyNowState()) {
                 ApplyNowState.STORE_CARD -> MyAccountSection.StoreCardLanding
                   ApplyNowState.PERSONAL_LOAN -> MyAccountSection.PersonalLoanLanding
                   ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD  -> MyAccountSection.CreditCardLanding
                   else -> MyAccountSection.AccountLanding

               }

                myAccountsFragmentViewModel.myAccountsPresenter?.viewApplicationStatusLinkInExternalBrowser(applyNowSection, this)
            }

        }
    }

    override fun invoke(position: Int) {
        with(binding.incAccountSalesFrontLayout.accountSalesCardHeader) {
            when (position) {
                CreditCardType.GOLD_CREDIT_CARD.ordinal -> {
                    val goldCreditCard = mAccountSalesModelImpl?.getCreditCard()?.get(position)
                    displayHeaderItems(goldCreditCard?.cardHeader)
                    cardFrontImageView?.visibility = VISIBLE
                    cardFrontBlackImageView?.visibility = GONE
                    goldCreditCard?.cardHeader?.drawables?.get(1)?.let { drawable ->
                        cardFrontImageView?.setImageResource(
                            drawable
                        )
                    }
                }
                CreditCardType.BLACK_CREDIT_CARD.ordinal -> {
                    val blackCreditCard = mAccountSalesModelImpl?.getCreditCard()?.get(position)
                    displayHeaderItems(blackCreditCard?.cardHeader)
                    cardFrontImageView?.visibility = INVISIBLE
                    cardFrontBlackImageView?.visibility = VISIBLE
                    blackCreditCard?.cardHeader?.drawables?.get(1)?.let { drawable ->
                        cardFrontBlackImageView?.setImageResource(
                            drawable
                        )
                    }
                }
                else -> throw RuntimeException("Invalid View Pager Page Selected ")
            }
        }
    }

    fun getCurrentFragment(position: Int) = (binding.incBottomSheetLayout.blackAndGoldCreditCardViewPager?.findFragmentAtPosition(supportFragmentManager, position) as? AccountSalesFragment)
}