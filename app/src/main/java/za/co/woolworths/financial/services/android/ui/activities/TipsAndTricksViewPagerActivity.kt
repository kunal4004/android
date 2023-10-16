 package za.co.woolworths.financial.services.android.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.res.TypedArray
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityTipsAndTricsViewPagerBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.MyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.APPLY_NOW_STATE
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.DEEP_LINKING_PARAMS
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl.Companion.MY_ACCOUNT_RESPONSE
import za.co.woolworths.financial.services.android.ui.adapters.TipsAndTricksViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities.ApplyNowActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.REQUEST_CODE_BARCODE_ACTIVITY
import kotlin.properties.Delegates

 class TipsAndTricksViewPagerActivity : AppCompatActivity(), View.OnClickListener, ViewPager.OnPageChangeListener {

     private lateinit var binding: ActivityTipsAndTricsViewPagerBinding
     private var tricksViewPagerAdapter: TipsAndTricksViewPagerAdapter? = null
     private var titles: Array<String>? = null
     private var descriptions: Array<String>? = null
     private var actionButtonTexts: Array<String>? = null
     private var icons: TypedArray by Delegates.notNull()
     private var mCurrentItem: Int = 0
     private var accountsResponse: AccountsResponse? = null
     private var availableAccounts: ArrayList<String> = arrayListOf()
     private val statementCCRedirect = "{\"productGroupCode\":\"CC\",\"feature\":\"Accounts Product Statement\"}"

    companion object {
        const val RESULT_OK_PRODUCTS = 123
        const val RESULT_OK_BARCODE_SCAN = 203
        const val RESULT_OK_ACCOUNTS = 234
        const val OPEN_SHOPPING_LIST_TAB_FROM_TIPS_AND_TRICK_RESULT_CODE = 3333
        const val RESULT_OK_REWARDS = 345
        const val REQUEST_CODE_DELIVERY_LOCATION = 456
        const val REQUEST_CODE_SHOPPING_LIST = 567
        const val RESULT_OK_OPEN_CART_FROM_TIPS_AND_TRICKS = 9999
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTipsAndTricsViewPagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Utils.updateStatusBarBackground(this, R.color.unavailable_color)
        binding.initViews()
        binding.setActionBar()
        QueryBadgeCounter.instance.queryVoucherCount()
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.TIPS_AND_TRICKS_DETAILS)
    }

    private fun ActivityTipsAndTricsViewPagerBinding.setActionBar() {
        setSupportActionBar(mToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    private fun ActivityTipsAndTricsViewPagerBinding.initViews() {
        next.setOnClickListener(this@TipsAndTricksViewPagerActivity)
        previous.setOnClickListener(this@TipsAndTricksViewPagerActivity)
        viewPager.addOnPageChangeListener(this@TipsAndTricksViewPagerActivity)
        featureActionButton.setOnClickListener(this@TipsAndTricksViewPagerActivity)
        titles = resources.getStringArray(R.array.tips_tricks_titles)
        descriptions = resources.getStringArray(R.array.tips_tricks_descriptions)
        icons = resources.obtainTypedArray(R.array.tips_tricks_icons)
        actionButtonTexts = resources.getStringArray(R.array.tips_tricks_buttons)
        bindDataToViews()
    }

    private fun ActivityTipsAndTricsViewPagerBinding.bindDataToViews() {
        mCurrentItem = intent.getIntExtra("position", 0)
        if (intent.hasExtra("accounts"))
            accountsResponse = Gson().fromJson(intent.extras?.getString("accounts"), AccountsResponse::class.java)
        tricksViewPagerAdapter = TipsAndTricksViewPagerAdapter(this@TipsAndTricksViewPagerActivity)
        viewPager.adapter = tricksViewPagerAdapter
        viewPager.currentItem = mCurrentItem
        onPageSelected(mCurrentItem)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.next -> {
                var current: Int = binding.viewPager.currentItem + 1
                if (current < titles!!.size) binding.viewPager?.currentItem = current else onBackPressed()
            }
            R.id.previous -> {
                val current: Int = binding.viewPager.currentItem
                binding.viewPager?.currentItem = current - 1
            }
            R.id.featureActionButton -> {
                when (binding.viewPager?.currentItem) {
                //NAVIGATION
                    0 -> {
                        if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.cartCount > 0) {
                            setResult(RESULT_OK_OPEN_CART_FROM_TIPS_AND_TRICKS)
                        } else {
                            setResult(RESULT_OK_PRODUCTS)
                        }
                        onBackPressed()
                    }
                //BARCODE SCAN
                    1 -> {
                        val openBarcodeActivity = Intent(this, BarcodeScanActivity::class.java)
                        startActivityForResult(openBarcodeActivity, REQUEST_CODE_BARCODE_ACTIVITY)
                        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
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
                        if (SessionUtilities.getInstance().isUserAuthenticated) {
                            presentAccounts()
                        }else {
                            redirectToMyAccountsCardsActivity(ApplyNowState.STORE_CARD)
                        }
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
        with(binding) {
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
                    featureTitle?.text = if (SessionUtilities.getInstance().isUserAuthenticated) resources.getString(R.string.tips_tricks_get_shopping) else titles?.get(position)
                    featureActionButton?.text = if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.cartCount > 0) resources.getString(R.string.tips_tricks_view_cart) else actionButtonTexts?.get(position)
                    featureDescription?.text = if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.cartCount > 0) resources.getString(R.string.tips_tricks_desc_navigation) else descriptions?.get(position)
                }
                2, 3 -> {
                    featureActionButton?.visibility = View.INVISIBLE
                }
                5 -> {
                    featureTitle?.text = if (SessionUtilities.getInstance().isUserAuthenticated) resources.getString(R.string.tips_tricks_your_vouchers) else titles?.get(position)
                    featureActionButton?.visibility = if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.voucherCount > 0) View.VISIBLE else View.INVISIBLE
                }
                6 -> {
                    featureTitle?.text =  if (SessionUtilities.getInstance().isUserAuthenticated) resources.getString(R.string.tips_tricks_view_your_accounts) else titles?.get(position)
                    featureActionButton?.text = resources?.getString(R.string.walkthrough_account_action_no_products)
                    featureActionButton.visibility = View.VISIBLE
                }
                7 -> {
                    featureTitle?.text = if (SessionUtilities.getInstance().isUserAuthenticated) resources.getString(R.string.tips_tricks_access_your_statements) else titles?.get(position)
                    featureActionButton?.visibility = if (SessionUtilities.getInstance().isUserAuthenticated && accountsResponse != null && ((getAvailableAccounts().contains(AccountsProductGroupCode.STORE_CARD.groupCode))
                                    || getAvailableAccounts().contains(AccountsProductGroupCode.PERSONAL_LOAN.groupCode))) View.VISIBLE else View.INVISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
        } else if (requestCode == REQUEST_CODE_BARCODE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            onBackPressed()
        } else if (requestCode == MyAccountActivity.REQUEST_CODE_MY_ACCOUNT_FRAGMENT) {
            if (resultCode == MyAccountActivity.RESULT_CODE_MY_ACCOUNT_FRAGMENT) {
                setResult(MyAccountActivity.RESULT_CODE_MY_ACCOUNT_FRAGMENT)
                finish()
                overridePendingTransition(0, 0)
            }
        }
    }

    private fun presentEditDeliveryLocation() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(this, 0)
        } else {
            ScreenManager.presentSSOSignin(this, REQUEST_CODE_DELIVERY_LOCATION)
        }
    }


    private fun presentShoppingList() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            setResult(OPEN_SHOPPING_LIST_TAB_FROM_TIPS_AND_TRICK_RESULT_CODE)
            onBackPressed()
        } else {
            ScreenManager.presentSSOSignin(this, REQUEST_CODE_SHOPPING_LIST)
        }
    }

    private fun presentAccounts() {
        availableAccounts = getAvailableAccounts()
        if (availableAccounts.size == 0) {
            redirectToMyAccountLandingPage(0)
        } else {
            when {
                availableAccounts.contains(AccountsProductGroupCode.STORE_CARD.groupCode) -> redirectToMyAccountLandingPage(0)
                availableAccounts.contains(AccountsProductGroupCode.CREDIT_CARD.groupCode) -> redirectToMyAccountLandingPage(1)
                availableAccounts.contains(AccountsProductGroupCode.PERSONAL_LOAN.groupCode) -> redirectToMyAccountLandingPage(2)
            }
        }
    }

     private fun presentAccountStatements() {
         val productGroupCode = AccountsProductGroupCode.getEnum(availableAccounts[0])
         availableAccounts = getAvailableAccounts()
         redirectToStatement( when(productGroupCode){
             AccountsProductGroupCode.STORE_CARD -> ApplyNowState.STORE_CARD
             AccountsProductGroupCode.PERSONAL_LOAN -> ApplyNowState.PERSONAL_LOAN
             else -> ApplyNowState.STORE_CARD
         })
     }

    private fun redirectToMyAccountLandingPage(position: Int) {
        val intent = Intent(this, MyAccountActivity::class.java)
        intent.putExtra("position", position)
        if (accountsResponse != null) {
            intent.putExtra("accounts", Utils.objectToJson(accountsResponse))
        }
        startActivityForResult(intent,MyAccountActivity.REQUEST_CODE_MY_ACCOUNT_FRAGMENT)
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
    }

    private fun getAvailableAccounts(): ArrayList<String> {
        availableAccounts.clear()
        accountsResponse?.accountList?.forEach {
            it.productGroupCode?.uppercase()?.let { it1 -> availableAccounts.add(it1) }
        }
        return availableAccounts
    }

     private fun redirectToMyAccountsCardsActivity(applyNowState: ApplyNowState) {
         val intent = Intent(this@TipsAndTricksViewPagerActivity, ApplyNowActivity::class.java)
         val bundle = Bundle()
         bundle.putSerializable("APPLY_NOW_STATE", applyNowState)
         bundle.putString("ACCOUNT_INFO", Gson().toJson(accountsResponse))
         intent.putExtras(bundle)
         startActivity(intent)
         overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
     }

     private fun redirectToStatement(applyNowState: ApplyNowState) {
        when(applyNowState){
            ApplyNowState.STORE_CARD -> navigateToStatementActivity(product = null, applyNowState = applyNowState)
            ApplyNowState.PERSONAL_LOAN -> navigateToStatementActivity(applyNowState = applyNowState)
            else -> {
                val intent = Intent(this@TipsAndTricksViewPagerActivity, AccountSignedInActivity::class.java)
                intent.putExtra(APPLY_NOW_STATE, applyNowState)
                intent.putExtra(DEEP_LINKING_PARAMS, statementCCRedirect)
                intent.putExtra(MY_ACCOUNT_RESPONSE, Utils.objectToJson(accountsResponse))
                startActivity(intent)
             }
         }
 }

 private fun navigateToStatementActivity(product: Account?, applyNowState:ApplyNowState) {
     statementsEvent(this,applyNowState)
         //TODO:: getStatement using offerid from WoolworthsApplication class, so we follow same approach to avoid impact of changing.
         // to be changed when we refactor statements activity and fragment
     val account = account(applyNowState)
     product?.let { WoolworthsApplication.getInstance().setProductOfferingId(it.productOfferingId) }
         val mAccountPair: Pair<ApplyNowState, Account?> = Pair(applyNowState,account)
         Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS, this)
         val openStatement = Intent(this, StatementActivity::class.java)
         openStatement.putExtra(ChatFragment.ACCOUNTS, Gson().toJson(mAccountPair))
         startActivity(openStatement)
         overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
 }

     private fun account(applyNowState: ApplyNowState): Account? {
         val account = accountsResponse?.accountList?.let {
             it.firstOrNull { account ->
                 account.productGroupCode?.equals(
                     applyNowState.name,
                     ignoreCase = true
                 ) == true
             }
         }
         return account
     }

     fun navigateToStatementActivity(applyNowState:ApplyNowState) {
         val product = account(applyNowState = applyNowState)
         val openStatement = Intent(this, StatementActivity::class.java)
         openStatement.putExtra(ChatFragment.ACCOUNTS, Gson().toJson(Pair(applyNowState, product)))
         startActivity(openStatement)
         overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
     }
 private fun statementsEvent(activity: Activity?,applyNowState: ApplyNowState) {
     when (applyNowState) {
         ApplyNowState.STORE_CARD -> {
             activity?.apply {
                 Utils.triggerFireBaseEvents(
                     FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS,
                     this
                 )
             }

         }

         ApplyNowState.PERSONAL_LOAN -> {
             activity?.apply {
                 Utils.triggerFireBaseEvents(
                     FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANSTATEMENTS,
                     this
                 )
             }
         }

         else -> Unit
     }
 }

 }