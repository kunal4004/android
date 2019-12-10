package za.co.woolworths.financial.services.android.ui.activities.account

import android.graphics.Typeface
import android.os.Bundle
import android.view.View.*
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.account_sales_activity.*
import kotlinx.android.synthetic.main.account_sales_front_layout.*
import za.co.woolworths.financial.services.android.contracts.AccountSalesContract
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader
import za.co.woolworths.financial.services.android.ui.fragments.account.AccountSalesFragment
import za.co.woolworths.financial.services.android.ui.views.SlidingUpPanelLayout
import za.co.woolworths.financial.services.android.util.KotlinUtils

private var mAccountSalesModelImpl: AccountSalesPresenterImpl? = null

class AccountSalesActivity : AppCompatActivity(), AccountSalesContract.AccountSalesView {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_sales_activity)
        KotlinUtils.setTransparentStatusBar(this)
        mAccountSalesModelImpl = AccountSalesPresenterImpl(this, AccountSalesModelImpl())
        val selectedBundle = intent?.extras?.getSerializable("APPLY_NOW_STATE")
        (selectedBundle as? ApplyNowState)?.let { state -> mAccountSalesModelImpl?.switchAccountSalesProduct(state) }

        navigateBackImageButton?.setOnClickListener { onBackPressed() }

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                updateTabFont(tab?.position ?: 0, false)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTabFont(tab?.position ?: 0, true)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        sliding_layout?.apply {
            anchorPoint = 0.3f
            panelHeight = mAccountSalesModelImpl?.getOverlayAnchoredHeight() ?: 0
            panelState = SlidingUpPanelLayout.PanelState.ANCHORED
        }

        storeCardApplyNowButton?.setOnClickListener { mAccountSalesModelImpl?.onApplyNowButtonTapped(this) }
    }

    override fun displayAccountSalesBlackInfo(storeCard: AccountSales) {
        updateAccountSalesDetail(storeCard)
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

    override fun displayCreditCard(goldCreditCard: AccountSales, blackCreditCard: AccountSales, position: Int) {
        nav_host_fragment?.view?.visibility = GONE
        blackAndGoldCreditCardViewPager?.visibility = VISIBLE
        blackAndGoldCreditCardViewPager?.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> AccountSalesFragment.newInstance(goldCreditCard)
                    1 -> AccountSalesFragment.newInstance(blackCreditCard)
                    else -> throw RuntimeException("Invalid Black/Gold Card Fragment Instance Index")
                }
            }

            override fun getItemCount(): Int = 2
        }

        TabLayoutMediator(tabLayout, blackAndGoldCreditCardViewPager) { tab, index ->
            tab.text = when (index) {
                0 -> getString(R.string.credit_card_gold_title)
                1 -> getString(R.string.credit_card_black_title)
                else -> throw RuntimeException("Invalid Account Gold/Black Title")
            }
        }.attach()

        blackAndGoldCreditCardViewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        displayHeaderItems(goldCreditCard.cardHeader)
                        cardFrontImageView?.visibility = VISIBLE
                        cardFrontBlackImageView?.visibility = GONE
                        goldCreditCard.cardHeader.drawables[1].let { drawable -> cardFrontImageView?.setImageResource(drawable) }
                    }
                    1 -> {
                        displayHeaderItems(blackCreditCard.cardHeader)
                        cardFrontImageView?.visibility = INVISIBLE
                        cardFrontBlackImageView?.visibility = VISIBLE
                        blackCreditCard.cardHeader.drawables[1].let { drawable -> cardFrontBlackImageView?.setImageResource(drawable) }
                    }
                    else -> throw RuntimeException("Invalid View Pager Page Selected ")
                }
            }
        })

        blackAndGoldCreditCardViewPager?.currentItem = position
    }

    private fun updateTabFont(position: Int, tabIsSelected: Boolean) {
        (((tabLayout?.getChildAt(0) as? ViewGroup)?.getChildAt(position) as? LinearLayout)?.getChildAt(1) as? AppCompatTextView)?.setTypeface(ResourcesCompat.getFont(this@AccountSalesActivity, if (tabIsSelected) R.font.futura_semi_bold_ttf else R.font.futura_medium_ttf), Typeface.NORMAL)
    }

    private fun updateAccountSalesDetail(storeCard: AccountSales) = mAccountSalesModelImpl?.setAccountSalesDetailPage(storeCard, findNavController(R.id.nav_host_fragment))

    override fun onBackPressed() {
        mAccountSalesModelImpl?.onBackPressed(this@AccountSalesActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAccountSalesModelImpl?.onDestroy()
    }
}