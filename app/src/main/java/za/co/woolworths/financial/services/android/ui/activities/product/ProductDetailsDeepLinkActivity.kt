package za.co.woolworths.financial.services.android.ui.activities.product

import android.app.ActivityManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_deeplink_pdp.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.startup.view.StartupActivity
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.startup.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.ui.activities.dashboard.ProductDetailsExtension
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity.Companion.DEEP_LINK_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

/**
 * Created by Kunal Uttarwar on 26/3/21.
 */
class ProductDetailsDeepLinkActivity : AppCompatActivity(), ProductDetailsExtension.ProductDetailsStatusListner {

    private lateinit var startupViewModel: StartupViewModel
    private lateinit var jsonLinkData: JsonObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deeplink_pdp)
        startProgressBar()
        var bundle: Any? = intent?.data
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
        } else
            bundle = appLinkData as Bundle
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
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_NATIVE_SHARE_DP_LNK, arguments, this)

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
        this.startActivity(intent)
        finishAffinity()
    }

    private fun goToProductDetailsActivity(bundle: Bundle?) {
        if (productDetailsprogressBar.isVisible)
            productDetailsprogressBar.visibility = View.GONE
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtras(bundle!!)
        startActivityForResult(intent, DEEP_LINK_REQUEST_CODE)
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
        if (requestCode == DEEP_LINK_REQUEST_CODE)
            finish()

    }
}