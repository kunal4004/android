package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_tips_and_trics_view_pager.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksViewPagerAdapter
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import kotlin.properties.Delegates

class TipsAndTricksViewPagerActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

    private var tricksViewPagerAdapter: TipsAndTricksViewPagerAdapter? = null
    private var titles: Array<String>? = null
    private var descriptions: Array<String>? = null
    private var actionButtonTexts: Array<String>? = null
    private var icons: TypedArray by Delegates.notNull()
    private var mCurrentItem: Int = 0
    private var accountsResponse: AccountsResponse? = null
    private var availableAccounts: ArrayList<String> = arrayListOf()

    companion object {
        const val RESULT_OK_PRODUCTS = 123
        const val RESULT_OK_ACCOUNTS = 234
        const val RESULT_OK_REWARDS = 345
        const val REQUEST_CODE_DELIVERY_LOCATION = 456
        const val REQUEST_CODE_SHOPPING_LIST = 567
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_and_trics_view_pager)
        Utils.updateStatusBarBackground(this, R.color.unavailable_color);
        initViews()
        setActionBar()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TIPS_AND_TRICKS_DETAILS)
    }

    private fun setActionBar() {
        setSupportActionBar(mToolbar);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false);
        supportActionBar?.setDisplayUseLogoEnabled(false);
        supportActionBar?.setHomeAsUpIndicator(R.drawable.back24);

    }

    private fun initViews() {
        next.setOnClickListener(this)
        previous.setOnClickListener(this)
        viewPager.addOnPageChangeListener(this)
        featureActionButton.setOnClickListener(this)
        titles = resources.getStringArray(R.array.tips_tricks_titles)
        descriptions = resources.getStringArray(R.array.tips_tricks_descriptions)
        icons = resources.obtainTypedArray(R.array.tips_tricks_icons)
        actionButtonTexts = resources.getStringArray(R.array.tips_tricks_buttons)
        bindDataToViews()
    }

    private fun bindDataToViews() {
        mCurrentItem = intent.getIntExtra("position", 0)
        if (intent.hasExtra("accounts"))
            accountsResponse = Gson().fromJson(intent.extras!!.getString("accounts"), AccountsResponse::class.java)
        tricksViewPagerAdapter = TipsAndTricksViewPagerAdapter(this)
        viewPager.adapter = tricksViewPagerAdapter
        viewPager.currentItem = mCurrentItem
        onPageSelected(mCurrentItem)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next -> {
                var current: Int = viewPager.currentItem + 1
                if (current < titles!!.size) viewPager.setCurrentItem(current) else onBackPressed()
            }
            R.id.previous -> {
                var current: Int = viewPager.currentItem
                viewPager.setCurrentItem(current - 1)
            }
            R.id.featureActionButton -> {
                when (viewPager.currentItem) {
                //NAVIGATION
                    0 -> {
                        if (SessionUtilities.getInstance().isUserAuthenticated() && QueryBadgeCounter.getInstance().cartCount > 0) {
                            startActivity(Intent(this, CartActivity::class.java))
                        } else {
                            setResult(RESULT_OK_PRODUCTS)
                            onBackPressed()
                        }
                    }
                //BARCODE SCAN
                    1 -> {
                        setResult(RESULT_OK_PRODUCTS)
                        onBackPressed()
                    }
                //DELIVERY LOCATION
                    4 -> {
                        presentEditDeliveryLocation()
                    }
                //VOUCHERS
                    5 -> {
                        setResult(RESULT_OK_REWARDS)
                        onBackPressed()
                    }
                //MY ACCOUNTS
                    6 -> {
                        presentAccounts()
                    }
                //STATEMENTS
                    7 -> {
                        presentAccountStatements()
                    }
                //SHOPPING LIST
                    8 -> {
                        presentShoppingList()
                    }
                }
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        featureActionButton.visibility = View.VISIBLE
        previous.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        next.setText(if ((position + 1) == titles?.size) resources.getString(R.string.done) else resources.getString(R.string.next))
        featureTitle.text = titles?.get(position)
        featureDescription.text = descriptions?.get(position)
        featureActionButton.text = actionButtonTexts?.get(position)
        counter.text = (position + 1).toString() + " OF " + titles?.size.toString()
        featureIcon.setBackgroundResource(icons.getResourceId(position, -1))
        when (position) {
            0->{
                featureTitle.text = if (SessionUtilities.getInstance().isUserAuthenticated()) resources.getString(R.string.tips_tricks_get_shopping) else titles?.get(position)
                featureActionButton.text = if (SessionUtilities.getInstance().isUserAuthenticated() && QueryBadgeCounter.getInstance().cartCount > 0) resources.getString(R.string.tips_tricks_view_cart) else actionButtonTexts?.get(position)
            }
            2, 3 -> {
                featureActionButton.visibility = View.INVISIBLE
            }
            5 -> {
                featureTitle.text = if (SessionUtilities.getInstance().isUserAuthenticated()) resources.getString(R.string.tips_tricks_your_vouchers) else titles?.get(position)
                featureActionButton.visibility = if (SessionUtilities.getInstance().isUserAuthenticated() && QueryBadgeCounter.getInstance().voucherCount > 0) View.VISIBLE else View.INVISIBLE
            }
            6 -> {
                featureTitle.text = if (SessionUtilities.getInstance().isUserAuthenticated()) resources.getString(R.string.tips_tricks_view_your_accounts) else titles?.get(position)
                featureActionButton.visibility = if (SessionUtilities.getInstance().isUserAuthenticated() && accountsResponse != null) View.VISIBLE else View.INVISIBLE
            }
            7 -> {
                featureTitle.text = if (SessionUtilities.getInstance().isUserAuthenticated()) resources.getString(R.string.tips_tricks_access_your_statements) else titles?.get(position)
                featureActionButton.visibility = if (SessionUtilities.getInstance().isUserAuthenticated() && accountsResponse != null && ((getAvailableAccounts().contains("SC")) || getAvailableAccounts().contains("PL"))) View.VISIBLE else View.INVISIBLE
            }

        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == SSOActivity.SSOActivityResult.SUCCESS.rawValue()) {
            when (requestCode) {
                REQUEST_CODE_DELIVERY_LOCATION -> {
                    presentEditDeliveryLocation()
                }
                REQUEST_CODE_SHOPPING_LIST -> {
                    presentShoppingList()
                }
            }
        }
    }

    private fun presentEditDeliveryLocation() {
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            startActivity(Intent(this, DeliveryLocationSelectionActivity::class.java))
            overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
        } else {
            ScreenManager.presentSSOSignin(this, REQUEST_CODE_DELIVERY_LOCATION)
        }
    }


    private fun presentShoppingList() {
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            setResult(RESULT_OK_ACCOUNTS)
            onBackPressed()
        } else {
            ScreenManager.presentSSOSignin(this, REQUEST_CODE_SHOPPING_LIST)
        }
    }

    private fun presentAccounts() {
        availableAccounts = getAvailableAccounts()
        if (availableAccounts.size == 0) {
            redirectToMyAccountsCardsActivity(0)
        } else {
            if (availableAccounts.contains("SC"))
                redirectToMyAccountsCardsActivity(0)
            else if (availableAccounts.contains("CC"))
                redirectToMyAccountsCardsActivity(1)
            else if (availableAccounts.contains("PL"))
                redirectToMyAccountsCardsActivity(2)
        }
    }

    private fun presentAccountStatements() {
        availableAccounts = getAvailableAccounts()
        if (availableAccounts.contains("SC"))
            redirectToMyAccountsCardsActivity(0)
        else if (availableAccounts.contains("PL"))
            redirectToMyAccountsCardsActivity(2)
    }

    fun redirectToMyAccountsCardsActivity(position: Int) {
        val intent = Intent(this, MyAccountCardsActivity::class.java)
        intent.putExtra("position", position)
        if (accountsResponse != null) {
            intent.putExtra("accounts", Utils.objectToJson(accountsResponse))
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    fun getAvailableAccounts(): ArrayList<String> {
        availableAccounts.clear()
        accountsResponse?.accountList?.forEach() {
            availableAccounts.add(it.productGroupCode.toUpperCase())
        }
        return availableAccounts;
    }
}
