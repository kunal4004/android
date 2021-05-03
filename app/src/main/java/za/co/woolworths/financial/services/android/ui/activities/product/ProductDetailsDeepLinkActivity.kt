package za.co.woolworths.financial.services.android.ui.activities.product

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_deeplink_pdp.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.item_limits.ProductCountMap
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.startup.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.CartActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.OPEN_CART_REQUEST
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity.Companion.DEEP_LINK_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity.Companion.SHARE_LINK_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.views.ToastFactory.Companion.showItemsLimitToastOnAddToCart
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.isDeliveryOptionClickAndCollect
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import java.util.*

/**
 * Created by Kunal Uttarwar on 26/3/21.
 */
class ProductDetailsDeepLinkActivity : AppCompatActivity(), ProductDetailsExtension.ProductDetailsStatusListner, ToastInterface {

    private lateinit var startupViewModel: StartupViewModel
    private lateinit var jsonLinkData: JsonObject
    private var mToastUtils: ToastUtils? = null
    private var deepLinkRequestCode = DEEP_LINK_REQUEST_CODE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink_pdp)
        startProgressBar()
        var bundle: Any? = intent?.data
        if (intent.hasExtra("deepLinkRequestCode")) {
            deepLinkRequestCode = intent?.getIntExtra("deepLinkRequestCode", DEEP_LINK_REQUEST_CODE)!!
        } else
            deepLinkRequestCode = SHARE_LINK_REQUEST_CODE
        if (intent.hasExtra("parameters")) {
            val parameter = intent?.getStringExtra("parameters")
            jsonLinkData = Utils.strToJson(parameter, JsonObject::class.java) as JsonObject
            bundle = Uri.parse(jsonLinkData.get("url").asString)
            intent?.action = Intent.ACTION_VIEW
        } else if (bundle == null && intent?.extras != null) {
            bundle = intent!!.extras!!
            intent?.action = Intent.ACTION_VIEW
        }
        if (Intent.ACTION_VIEW == intent?.action && bundle != null && bundle.toString().contains("A-")) {
            handleAppLink(bundle)
        } else {
            goToProductDetailsActivity(bundle as Bundle?)
        }
    }

    private fun parseDeepLinkData(bundle: Bundle) {
        val deepLinkData: String = bundle?.getString("parameters", "").replace("\\", "")
        jsonLinkData = Utils.strToJson(deepLinkData, JsonObject::class.java) as JsonObject
    }

    private fun handleAppLink(appLinkData: Any?) {
        var bundle: Bundle
        if (appLinkData is Uri) {
            bundle = bundleOf(
                    "feature" to AppConstant.DP_LINKING_PRODUCT_DETAIL,
                    "parameters" to "{\"url\": \"${appLinkData}\"}"
            )
        } else {
            bundle = appLinkData as Bundle
        }
        parseDeepLinkData(bundle)

        if (bundle != null && bundle.get("feature") != null && !TextUtils.isEmpty(bundle.get("feature").toString()) && jsonLinkData?.get("url") != null) {
            val linkData = Uri.parse(jsonLinkData.get("url").asString)
            val productSearchTerm = linkData.pathSegments?.find { it.startsWith("A-") }!!
            if (productSearchTerm == null || productSearchTerm.isEmpty()) {
                restartApp()
            }

            val productId = productSearchTerm.substring(2)
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID] = productId
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE] = FirebaseManagerAnalyticsProperties.ACTION_PDP_DEEPLINK
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_NATIVE_SHARE_DP_LNK, arguments)

            setupViewModel()
            init()
            val defaultLocation = WoolworthsApplication.getQuickShopDefaultValues()
            if (defaultLocation == null)
                getConfig(productId)
            else
                ProductDetailsExtension.retrieveProduct(productId, productId, this, this)
        } else {
            finish()
        }
    }

    private fun setupViewModel() {
        startupViewModel = ViewModelProviders.of(
                this,
                ViewModelFactory(StartUpRepository(StartupApiHelper()), StartupApiHelper())
        ).get(StartupViewModel::class.java)
    }

    private fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this)
        }
        // Disable first time launch splash video screen, remove to enable video on startup
        startupViewModel.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        startupViewModel.setUpEnvironment(this@ProductDetailsDeepLinkActivity)
        if (startupViewModel.isConnectedToInternet(this@ProductDetailsDeepLinkActivity)) {
            startupViewModel.setUpFirebaseEvents()
        }
        //Remove old usage of SharedPreferences data.
        startupViewModel.clearSharedPreference(this@ProductDetailsDeepLinkActivity)
        AuthenticateUtils.getInstance(this@ProductDetailsDeepLinkActivity).enableBiometricForCurrentSession(true)
    }

    private fun getConfig(productId: String) {
        startupViewModel.queryServiceGetConfig().observe(this, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    stopProgressBar()
                    ConfigResource.persistGlobalConfig(it.data, startupViewModel)
                    ProductDetailsExtension.retrieveProduct(productId, productId, this, this)
                }
                ResponseStatus.LOADING -> {
                    startProgressBar()
                }
                ResponseStatus.ERROR -> {
                    stopProgressBar()
                }
            }
        })
    }

    private fun restartApp() {
        val intent = Intent(this, StartupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun goToProductDetailsActivity(bundle: Bundle?) {
        if (productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.GONE
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtras(bundle!!)
        startActivityForResult(intent, deepLinkRequestCode)
        overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
    }

    override fun onSuccess(bundle: Bundle) {
        goToProductDetailsActivity(bundle)
    }

    override fun onFailure() {
        stopProgressBar()
    }

    override fun onProductNotFound(message: String) {
        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = mngr!!.getRunningTasks(10)
        if (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.localClassName
                && taskList.get(0).baseActivity?.className == ProductDetailsDeepLinkActivity::class.java.name) {
            restartApp()
        } else {
            finish()
            overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
        }
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun startProgressBar() {
        if (!productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.VISIBLE
    }

    override fun stopProgressBar() {
        if (productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            DEEP_LINK_REQUEST_CODE -> {
                if (isDeepLinkActivity()) {
                    restartApp()
                } else if (isHavingBottomNavigationActivity()) {
                    if (resultCode == RESULT_OK && data != null) {
                        GlobalScope.doAfterDelay(DelayConstant.DELAY_2000_MS) {
                            if (!(mToastUtils != null && mToastUtils?.isButtonClicked == true))
                                restartApp()
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        restartApp()
                    } else
                        finish()
                }
                data?.let { checkAndSetToastMessage(it, resultCode) }
            }

            SHARE_LINK_REQUEST_CODE -> {
                if (isDeepLinkActivity()) {
                    if (resultCode == RESULT_OK && data != null) {
                        GlobalScope.doAfterDelay(DelayConstant.DELAY_2000_MS) {
                            if (!(mToastUtils != null && mToastUtils?.isButtonClicked == true))
                                restartApp()
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        restartApp()
                    }
                } else if (isHavingBottomNavigationActivity()) {
                    if (resultCode == RESULT_OK && data != null) {
                    } else if (resultCode == RESULT_CANCELED) {
                        finish()
                    } else
                        restartApp()
                }
                data?.let { checkAndSetToastMessage(it, resultCode) }
            }

            OPEN_CART_REQUEST -> restartApp()
        }
    }

    private fun checkAndSetToastMessage(data: Intent, resultCode: Int) {
        if (resultCode == RESULT_OK && data != null) {
            val itemAddToCartMessage = data.getStringExtra("addedToCartMessage")
            val productCountMap = Utils.jsonStringToObject(data.getStringExtra("ProductCountMap"), ProductCountMap::class.java) as ProductCountMap
            val itemsCount = data.getIntExtra("ItemsCount", 0)
            if (itemAddToCartMessage != null) {
                setToast(itemAddToCartMessage, "", productCountMap, itemsCount)
            }
            setResult(RESULT_OK, data)
            GlobalScope.doAfterDelay(DelayConstant.DELAY_2000_MS) {
                if (!(mToastUtils != null && mToastUtils?.isButtonClicked == true))
                    finish()
            }
        } else {
            finish()
        }
    }

    private fun isDeepLinkActivity(): Boolean {
        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = mngr!!.getRunningTasks(10)
        return (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.localClassName
                && taskList.get(0).baseActivity?.className == ProductDetailsDeepLinkActivity::class.java.name)

    }

    private fun isHavingBottomNavigationActivity(): Boolean {
        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        val taskList = mngr!!.getRunningTasks(10)
        return (taskList[0].numActivities == 2 && taskList[0].topActivity!!.className == this.localClassName
                && taskList.get(0).baseActivity?.className == BottomNavigationActivity::class.java.name)
    }

    fun setToast(message: String?, cartText: String?, productCountMap: ProductCountMap?, noOfItems: Int) {
        if (productCountMap != null && isDeliveryOptionClickAndCollect() && productCountMap.quantityLimit!!.foodLayoutColour != null) {
            showItemsLimitToastOnAddToCart(pdpBottomNavigation, productCountMap, this, noOfItems, true)
            return
        }
        mToastUtils = ToastUtils(this@ProductDetailsDeepLinkActivity)
        mToastUtils?.apply {
            setActivity(this@ProductDetailsDeepLinkActivity)
            setView(pdpBottomNavigation)
            setGravity(Gravity.BOTTOM)
            setCurrentState(this.javaClass.simpleName)
            setCartText(cartText)
            setAllCapsUpperCase(false)
            setPixel(pdpBottomNavigation.getHeight() + Utils.dp2px(10f))
            setMessage(message)
            setViewState(true)
            build()
        }

    }

    override fun onToastButtonClicked(currentState: String?) {
        if (mToastUtils != null) {
            val state: String = mToastUtils!!.getCurrentState()
            if (currentState.equals(state, ignoreCase = true)) {
                mToastUtils?.setButtonClicked(true)
                // do anything when popupWindow was clicked
                if (!SessionUtilities.getInstance().isUserAuthenticated) {
                    ScreenManager.presentSSOSignin(this@ProductDetailsDeepLinkActivity)
                } else {
                    val openCartActivity = Intent(this@ProductDetailsDeepLinkActivity, CartActivity::class.java)
                    startActivityForResult(openCartActivity, BottomNavigationActivity.OPEN_CART_REQUEST)
                    overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                }
            }
        }
    }
}