package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.activity_tips_and_trics_view_pager.*
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

    companion object {
        const val RESULT_OK_PRODUCTS = 123
        const val RESULT_OK_ACCOUNTS = 234
        const val RESULT_OK_REWARDS = 345
        const val REQUEST_CODE_DELIVERY_LOCATION = 456
        const val REQUEST_CODE_STATEMENTS = 567
        const val REQUEST_CODE_SHOPPING_LIST = 678
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips_and_trics_view_pager)
        Utils.updateStatusBarBackground(this, R.color.unavailable_color);
        initViews()
        setActionBar()
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
                        val intent = Intent(this, MyAccountCardsActivity::class.java)
                        intent.putExtra("position", 0)
                        /*if (accountsResponse != null) {
                            intent.putExtra("accounts", Utils.objectToJson(accountsResponse))
                        }*/
                        startActivityForResult(intent, 0)
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
                    }
                //STATEMENTS
                    7 -> {
                        presentAccountStatements()
                    }
                //SHOPPING LIST
                    8 -> {
                        setResult(RESULT_OK_ACCOUNTS)
                        onBackPressed()
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
            5 -> {
                featureActionButton.visibility = if (SessionUtilities.getInstance().isUserAuthenticated() && QueryBadgeCounter.getInstance().voucherCount > 0) View.VISIBLE else View.INVISIBLE
            }
            6 -> {
                featureActionButton.visibility = if (SessionUtilities.getInstance().isUserAuthenticated()) View.VISIBLE else View.INVISIBLE
            }
            2, 3 -> {
                featureActionButton.visibility = View.INVISIBLE
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
                REQUEST_CODE_STATEMENTS -> {
                    presentAccountStatements()
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

    private fun presentAccountStatements() {
        if (SessionUtilities.getInstance().isUserAuthenticated()) {
            val intent = Intent(this, MyAccountCardsActivity::class.java)
            intent.putExtra("position", 0)
            /*if (accountsResponse != null) {
                intent.putExtra("accounts", Utils.objectToJson(accountsResponse))
            }*/
            startActivityForResult(intent, 0)
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
        } else {
            ScreenManager.presentSSOSignin(this, REQUEST_CODE_STATEMENTS)
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
}
