package za.co.woolworths.financial.services.android.startup.view

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivitySplashScreenBinding
import com.awfs.coordination.databinding.ActivityStartupBinding
import com.awfs.coordination.databinding.ActivityStartupResourcenotfoundBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.firebase.FirebaseConfigUtils
import za.co.woolworths.financial.services.android.firebase.model.ConfigData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant.Companion.startOCChatService
import za.co.woolworths.financial.services.android.onecartgetstream.service.DashChatMessageListeningService
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.startup.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.ui.wfs.common.biometric.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ImageManager
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.pushnotification.PushNotificationManager
import javax.inject.Inject

@AndroidEntryPoint
class StartupActivity :
    AppCompatActivity(),
    MediaPlayer.OnCompletionListener,
    View.OnClickListener {

    private lateinit var bindingStartup: ActivityStartupBinding
    private lateinit var bindingResourceNotFound: ActivityStartupResourcenotfoundBinding
    private lateinit var bindingSplash: ActivitySplashScreenBinding
    private lateinit var configBuilder: FirebaseRemoteConfigSettings.Builder
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var deeplinkIntent: Intent
    private var actionUrlFirst: String? = AppConstant.EMPTY_STRING
    private var actionUrlSecond: String? = AppConstant.EMPTY_STRING
    private var remoteConfigJsonString: String = AppConstant.EMPTY_STRING
    private var isAppSideLoaded = false

    @Inject
    lateinit var notificationUtils: NotificationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setUpFirebaseconfig()
        setupDataListener()

        try {
            // Try to get a drawable, to make sure the app has not
            // been sideloaded to a device with a different pixel density
            AppCompatResources.getDrawable(this, R.drawable.splash_w_logo)

            bindingStartup = ActivityStartupBinding.inflate(layoutInflater)
            setContentView(bindingStartup.root)
            setSupportActionBar(bindingStartup.mToolbar)

            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
            )
            if (supportActionBar?.isShowing == true) {
                supportActionBar?.hide()
            }
            bindingStartup.splashNoVideoView.progressBar?.indeterminateDrawable?.setColorFilter(
                Color.BLACK,
                PorterDuff.Mode.MULTIPLY,
            )
            bindingStartup.splashNoVideoView.retry?.setOnClickListener(this@StartupActivity)
            deeplinkIntent = intent
            init()
        } catch (e: Resources.NotFoundException) {
            // Consider the app has been sideloaded and the split APK doesn't have
            // the required drawables to run on this specific device
            bindingResourceNotFound = ActivityStartupResourcenotfoundBinding.inflate(layoutInflater)
            setContentView(bindingResourceNotFound.root)
            isAppSideLoaded = true
        }
    }

    private fun setupDataListener() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                startupViewModel.cartSummary.collect {
                    when (it.status) {
                        Status.LOADING -> {
                            setupLoadingScreen()
                        }
                        // In Cart summary success or failure proceed as before
                        else -> {
                            // Setting up Cart count on bottom tabs
                            it.data?.data?.getOrNull(0)?.apply {
                                if (totalItemsCount != null)
                                    QueryBadgeCounter.instance.setCartSummaryResponse(this)
                            }

                            when {
                                // When get config fails
                                !startupViewModel.isGetConfigSuccess ->{
                                    if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
                                        configureDashChatServices()
                                    }
                                    onConfigFailure()
                                }
                                else -> onConfigSuccess()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUpFirebaseconfig() {
        firebaseRemoteConfig = startupViewModel.getFirebaseRemoteConfigData()
        if (Utils.isAppUpdated(this)) {
            // Reset Firebase Remote Config cache if the app has been updated
            firebaseRemoteConfig.reset()
        }
        configBuilder = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(AppConstant.FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL)
            .setFetchTimeoutInSeconds(AppConstant.FIREBASE_REMOTE_CONFIG_TIMEOUT_INTERVAL)
        val defaultJsonString =
            FirebaseConfigUtils.getJsonDataFromAsset(this, FirebaseConfigUtils.FILE_NAME)
        val defaultValues = mutableMapOf(FirebaseConfigUtils.CONFIG_KEY to defaultJsonString)
        firebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build())
        firebaseRemoteConfig.setDefaultsAsync(defaultValues as Map<String, Any>)
    }

    private fun onRemoteConfigFetchComplete(isSuccessful: Boolean, isComingFromSuccess: Boolean) {
        if (isSuccessful) {
            // set dynamic ui here
            remoteConfigJsonString = startupViewModel.fetchFirebaseRemoteConifgData()
            if (isComingFromSuccess) {
                // success of api
                if (remoteConfigJsonString.isEmpty()) {
                    // api successful but firebase not configured so navigate with normal flow
                    presentNextScreenOrServerMessage()
                } else {
                    // api successful and  firebase also configured so display sunsetting ui
                    bindingSplash = ActivitySplashScreenBinding.inflate(layoutInflater)
                    setContentView(bindingSplash.root)
                    val configData: ConfigData? =
                            startupViewModel.parseRemoteconfigData(remoteConfigJsonString)
                    if (configData?.expiryTime == -1L || configData == null) {
                        // in case we get json exception while parsing then we navigate with normal flow
                        bindingSplash.progressBar?.visibility = View.GONE
                        presentNextScreenOrServerMessage()
                    } else {
                        bindingSplash.setDataOnUI(configData, true)
                    }
                }
            } else {
                // error  of api
                if (remoteConfigJsonString.isEmpty()) {
                    // api is  failed and firebase not configured so show error screen of api response
                    bindingStartup.showNonVideoViewWithErrorLayout()
                } else {
                    // api is failed and sunsetting is configured then show sunsetting ui

                    val configData: ConfigData? =
                            startupViewModel.parseRemoteconfigData(remoteConfigJsonString)
                    if (configData?.expiryTime == -1L || configData == null) {
                        // in case we get json exception while parsing then show error screen of api
                        bindingStartup.showNonVideoViewWithErrorLayout()
                    } else {
                        bindingSplash =
                                ActivitySplashScreenBinding.inflate(layoutInflater)
                        setContentView(bindingSplash.root)
                        bindingSplash.setDataOnUI(configData, false)
                    }
                }
            }
        } else {
            // firebase fail
            if (isComingFromSuccess) {
                // api is success and firebase  is failed so navigate to next screen
                presentNextScreenOrServerMessage()
            } else {
                // api is failed and firebase  is failed so display error layout
                bindingStartup.showNonVideoViewWithErrorLayout()
            }
        }
    }

    private fun fetchFirebaseConfigData(isComingFromSuccess: Boolean) {
        val isFirstTime = startupViewModel.getSessionDao(SessionDao.KEY.ON_BOARDING_SCREEN)
        if (isFirstTime) {
            firebaseRemoteConfig
                    .fetchAndActivate()
                    .addOnCompleteListener { task ->
                        run {
                            onRemoteConfigFetchComplete(task.isSuccessful, isComingFromSuccess)
                        }
                    }
        } else {
            firebaseRemoteConfig
                    .fetch(AppConstant.FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL)
                    .addOnCompleteListener { task ->
                        run {
                            if (task.isSuccessful)
                                firebaseRemoteConfig.activate()
                            onRemoteConfigFetchComplete(task.isSuccessful, isComingFromSuccess)
                        }
                    }
        }
    }

    private fun ActivitySplashScreenBinding.setDataOnUI(
        configData: ConfigData?,
        isComingFromSuccess: Boolean,
    ) {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.SPLASH_WITH_CTA)
        progressBar?.visibility = View.GONE
        firstBtn?.visibility = View.VISIBLE
        secondBtn?.visibility = View.VISIBLE
        firstBtn?.setOnClickListener(this@StartupActivity)
        secondBtn?.setOnClickListener(this@StartupActivity)

        val timeIntervalSince1970: Long = System.currentTimeMillis()

        if (configData != null) {
            if (timeIntervalSince1970 < configData.expiryTime) {
                val activeConfiguration = configData.activeConfiguration
                activeConfiguration?.run {
                    if (title == null) {
                        txtTitle?.visibility = View.GONE
                    } else {
                        txtTitle?.text = activeConfiguration.title
                    }

                    if (description == null) {
                        txtDesc?.visibility = View.GONE
                    } else {
                        txtDesc?.text = activeConfiguration.description
                    }

                    if (imageUrl == null) {
                        imgView?.visibility = View.GONE
                    } else {
                        if (imageUrl.isEmpty()) {
                            imgView.setImageResource(R.drawable.link_icon)
                        } else {
                            ImageManager.setPictureWithSplashPlaceHolder(imgView, imageUrl)
                        }
                    }

                    if (firstButton == null) {
                        firstBtn?.visibility = View.GONE
                    } else {
                        firstBtn?.text = firstButton.title
                        actionUrlFirst = firstButton.actionUrl
                    }

                    if (secondButton == null) {
                        secondBtn?.visibility = View.GONE
                    } else {
                        secondBtn?.text = secondButton.title
                        actionUrlSecond = secondButton.actionUrl
                    }
                }
            } else if (timeIntervalSince1970 >= configData.expiryTime && timeIntervalSince1970 != -1L) {
                val inActiveConfiguration = configData?.inactiveConfiguration
                inActiveConfiguration?.run {
                    if (title == null) {
                        txtTitle?.visibility = View.GONE
                    } else {
                        txtTitle?.text = inActiveConfiguration.title
                    }

                    if (description == null) {
                        txtDesc?.visibility = View.GONE
                    } else {
                        txtDesc?.text = inActiveConfiguration.description
                    }

                    if (imageUrl == null) {
                        imgView?.visibility = View.GONE
                    } else {
                        if (imageUrl.isEmpty()) {
                            imgView.setImageResource(R.drawable.link_icon)
                        } else {
                            ImageManager.setPictureWithSplashPlaceHolder(imgView, imageUrl)
                        }
                    }

                    if (firstButton == null) {
                        firstBtn?.visibility = View.GONE
                    } else {
                        firstBtn?.text = firstButton.title
                        actionUrlFirst = firstButton.actionUrl
                    }

                    if (secondButton == null) {
                        secondBtn?.visibility = View.GONE
                    } else {
                        secondBtn?.text = secondButton.title
                        actionUrlSecond = secondButton.actionUrl
                    }
                }
            } else if (configData.expiryTime == -1L && isComingFromSuccess) {
                presentNextScreenOrServerMessage()
            } else if (configData.expiryTime == -1L && !isComingFromSuccess) {
                bindingStartup.showNonVideoViewWithErrorLayout()
            }
        }
    }

    fun init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createNotificationChannelIfNeeded()
        }
        // Disable first time launch splash video screen, remove to enable video on startup
        startupViewModel.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        startupViewModel.setUpEnvironment(this@StartupActivity)
        if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
            startupViewModel.setUpFirebaseEvents()
        } else {
            bindingStartup.showNonVideoViewWithErrorLayout()
        }

        if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
            configureDashChatServices()
        }
        // Remove old usage of SharedPreferences data.
        //   startupViewModel.clearSharedPreference(this@StartupActivity)
        AuthenticateUtils.enableBiometricForCurrentSession(true)
    }

    private fun setupLoadingScreen() {
        if (isFirstTime()) {
            bindingStartup.showVideoView()
        } else {
            bindingStartup.showNonVideoViewWithoutErrorLayout()
        }
    }

    private fun isFirstTime(): Boolean {
        return startupViewModel.getSessionDao(SessionDao.KEY.SPLASH_VIDEO)
    }

    fun ActivityStartupBinding.showVideoView() {
        splashNoVideoView?.root?.visibility = View.GONE
        splashServerMessageView?.root?.visibility = View.GONE
        videoViewLayout?.visibility = View.VISIBLE

        val randomVideo = startupViewModel.randomVideoPath
        if (randomVideo.isNotEmpty()) {
            val videoUri = Uri.parse(randomVideo)
            activityWsplashScreenVideoview?.apply {
                setVideoURI(videoUri)
                start()
                setOnCompletionListener(this@StartupActivity)
            }
            startupViewModel.isVideoPlaying = false
        }
    }

    fun ActivityStartupBinding.showNonVideoViewWithErrorLayout() {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.STARTUP_API_ERROR)
        runOnUiThread {
            with(splashNoVideoView) {
                progressBar?.visibility = View.GONE
                splashNoVideoView?.root?.visibility = View.GONE
                splashNoVideoView?.root?.visibility = View.VISIBLE
                splashServerMessageView?.root?.visibility = View.GONE
                errorLayout?.visibility = View.VISIBLE
            }
        }
    }

    fun ActivityStartupBinding.showNonVideoViewWithoutErrorLayout() {
        splashNoVideoView.progressBar?.visibility = View.VISIBLE
        videoViewLayout?.visibility = View.GONE
        splashNoVideoView.errorLayout?.visibility = View.GONE
        splashNoVideoView?.root?.visibility = View.VISIBLE
        splashServerMessageView?.root?.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        startupViewModel.apply {
            when (v?.id) {
                R.id.retry -> {
                    if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
                        startupViewModel.setupFirebaseUserProperty()
                        getConfig()
                    } else {
                        bindingStartup.showNonVideoViewWithErrorLayout()
                    }
                }
                R.id.first_btn -> bindingSplash.handleFirstbuttonClick()
                R.id.second_btn -> bindingSplash.handleSecondbuttonClick()
            }
        }
    }

    private fun ActivitySplashScreenBinding.handleSecondbuttonClick() {
        val text: String = secondBtn?.text.toString()
        val updatedText: String = Utils.formatAnalyticsButtonText(text)
        if (!text.isEmpty()) {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SPLASH_BTN.plus(updatedText),
                this@StartupActivity,
            )
        }
        if (actionUrlSecond.isNullOrEmpty()) {
            presentNextScreen()
        } else {
            ScreenManager.presentToActionView(this@StartupActivity, actionUrlSecond)
        }
    }

    private fun ActivitySplashScreenBinding.handleFirstbuttonClick() {
        val text: String = firstBtn?.text.toString()
        val updatedText: String = Utils.formatAnalyticsButtonText(text)
        if (!text.isEmpty()) {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SPLASH_BTN.plus(updatedText),
                this@StartupActivity,
            )
        }
        if (actionUrlFirst.isNullOrEmpty()) {
            presentNextScreen()
        } else {
            ScreenManager.presentToActionView(this@StartupActivity, actionUrlFirst)
        }
    }

    fun getConfig() {
        startupViewModel.queryServiceGetConfig().observe(this) {
            when (it.responseStatus) {

                ResponseStatus.LOADING -> setupLoadingScreen()
                ResponseStatus.ERROR -> onConfigFailure()
                ResponseStatus.SUCCESS -> {

                    ConfigResource.persistGlobalConfig(it.data, startupViewModel)
                    startupViewModel.videoPlayerShouldPlay = false
                    if (TextUtils.isEmpty(it.data?.configs?.enviroment?.stsURI)) {
                        bindingStartup.showNonVideoViewWithErrorLayout()
                        return@observe
                    }
                    // Fixing https://woolworths.atlassian.net/browse/APP1-1923
                    // Makes cart summary API call to hit ATG and creates a single JSessionId
                    when {
                        SessionUtilities.getInstance().isUserAuthenticated ->
                            startupViewModel.queryCartSummary()

                        else -> onConfigSuccess()
                    }

                }
            }
        }
    }

    private fun onConfigSuccess() {
        if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
            configureDashChatServices()
        }
        if (!startupViewModel.isVideoPlaying) {
            if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
                fetchFirebaseConfigData(true)
            } else {
                bindingStartup.showNonVideoViewWithErrorLayout()
            }
        }
    }

    private fun onConfigFailure() {
        fetchFirebaseConfigData(false)
    }

    //video player on completion
    override fun onCompletion(mp: MediaPlayer?) {
        startupViewModel.apply {
            isVideoPlaying = false
            if (!videoPlayerShouldPlay) {
                presentNextScreenOrServerMessage()
                mp?.stop()
            } else {
                bindingStartup.showNonVideoViewWithoutErrorLayout()
            }
        }
    }

    fun presentNextScreenOrServerMessage() {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.SPLASH_WITHOUT_CTA)
        bindingStartup.showNonVideoViewWithoutErrorLayout()
        presentNextScreen()
    }

    private fun setupViewModel() {
        startupViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(StartUpRepository(StartupApiHelper()), StartupApiHelper()),
        ).get(StartupViewModel::class.java)
    }

    fun presentNextScreen() {
        val isFirstTime = startupViewModel.getSessionDao(SessionDao.KEY.ON_BOARDING_SCREEN)
        var appLinkData: Any? = deeplinkIntent.data

        AppConfigSingleton.isBadgesRequired =
            deeplinkIntent.extras?.containsKey("google.message_id") != true

        if (appLinkData == null && deeplinkIntent.extras != null) {
            appLinkData = deeplinkIntent.extras
            deeplinkIntent.action = Intent.ACTION_VIEW
        }

        if (Intent.ACTION_VIEW == deeplinkIntent.action && appLinkData != null) {
            handleAppLink(appLinkData)
        } else {
            val activity = this as Activity
            if (isFirstTime == null || Utils.isAppUpdated(this)) {
                ScreenManager.presentOnboarding(activity)
            } else {
                ScreenManager.presentMain(activity)
            }
        }
        // forgot password deeplink
        forgotPasswordDeeplink()
    }

    fun handleAppLink(appLinkData: Any?) {
        // val productSearchViewModel: ProductSearchViewModel = ProductSearchViewModelImpl();
        // productSearchViewModel.getTypeAndTerm(urlString = appLinkData.toString())
        // 1. check URL
        // 2. navigate to facet that URL corresponds to
        if (appLinkData is Uri) {
            val bundle = bundleOf(
                "feature" to AppConstant.DP_LINKING_PRODUCT_LISTING,
                "parameters" to "{\"url\": \"${appLinkData}\"}",
            )
            ScreenManager.presentMain(this@StartupActivity, bundle)
        } else if (appLinkData is Bundle && appLinkData.containsKey(AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID)) {
            // Push notification created by Messaging Service, when app was active and foreground
            val channelId = appLinkData[AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID] as String
            DashChatMessageListeningService.getOrderIdForChannel(
                this,
                channelId,
                onSuccess = { orderId ->
                    val bundle = bundleOf(
                        "feature" to AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID,
                        "parameters" to "{\"${AppConstant.DP_LINKING_PARAM_STREAM_ORDER_ID}\": \"${orderId}\", \"${AppConstant.DP_LINKING_PARAM_STREAM_CHANNEL_ID}\": \"${channelId}\"}",
                    )
                    ScreenManager.presentMain(this@StartupActivity, bundle)
                },
                onFailure = {
                    ScreenManager.presentMain(this@StartupActivity)
                },
            )
        } else if (appLinkData is Bundle && appLinkData.containsKey(PushNotificationManager.PAYLOAD_STREAM_CHANNEL)) {
            // Push notification created by OS, when app was inactive
            val streamChannelJson =
                appLinkData[PushNotificationManager.PAYLOAD_STREAM_CHANNEL] as String
            val streamChannelParameters = Gson().fromJson(
                streamChannelJson,
                JsonObject::class.java,
            )
            // Stream Channel's cid needs to be in the format channelType:channelId. For example, messaging:123
            val channelId =
                "${streamChannelParameters[PushNotificationManager.PAYLOAD_STREAM_CHANNEL_TYPE].asString}:${streamChannelParameters[PushNotificationManager.PAYLOAD_STREAM_CHANNEL_ID].asString}"
            DashChatMessageListeningService.getOrderIdForChannel(
                this,
                channelId,
                onSuccess = { orderId ->
                    val bundle = bundleOf(
                        "feature" to AppConstant.DP_LINKING_STREAM_CHAT_CHANNEL_ID,
                        "parameters" to "{\"${AppConstant.DP_LINKING_PARAM_STREAM_ORDER_ID}\": \"${orderId}\", \"${AppConstant.DP_LINKING_PARAM_STREAM_CHANNEL_ID}\": \"${channelId}\"}",
                    )
                    ScreenManager.presentMain(this@StartupActivity, bundle)
                },
                onFailure = {
                    ScreenManager.presentMain(this@StartupActivity)
                },
            )
        } else {
            ScreenManager.presentMain(this@StartupActivity, appLinkData as Bundle)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isAppSideLoaded) {
            Utils.setScreenName(
                FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_SIDELOADED_AT_STARTUP,
            )
        } else {
            if (Utils.checkForBinarySu() && CommonUtils.isRooted() && !Util.isDebug(
                    this.applicationContext,
                )
            ) {
                Utils.setScreenName(
                    FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_ROOTED_AT_STARTUP,
                )
                val rootedDeviceInfoFragment = newInstance(getString(R.string.rooted_phone_desc))
                rootedDeviceInfoFragment.show(
                    supportFragmentManager,
                    RootedDeviceInfoFragment::class.java.simpleName,
                )
                return
            }
            onStartInit()
        }
    }

    fun onStartInit() {
        startupViewModel.apply {
            if (isAppMinimized) {
                isAppMinimized = false
                if (isServerMessageShown) {
                    bindingStartup.showNonVideoViewWithoutErrorLayout()
                    getConfig()
                } else {
                    startActivity(Intent(this@StartupActivity, StartupActivity::class.java))
                    finish()
                }
            } else {
                if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
                    getConfig()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        startupViewModel.isAppMinimized = true
    }

    override fun onResume() {
        super.onResume()
        notificationUtils.clearNotifications()
    }

    private fun forgotPasswordDeeplink() {
        var uri = intent?.data
        if (null != uri) {
            var params = uri.pathSegments
            if (params.isNullOrEmpty()== false) {
                val forgotPassword = params[params.size - 1]
                if (null != forgotPassword && forgotPassword.contentEquals("forgot-password")) {
                    getForgotPasswordLink(uri.toString())
                }
            }
        }
    }

    private fun getForgotPasswordLink(forgotPasswordUri: String) {
        ScreenManager.forgotPassword(this@StartupActivity, forgotPasswordUri)
    }

    private fun configureDashChatServices() {
        try {
            if (FirebaseApp.getApps(this).none { it.name == getString(R.string.oc_chat_app) }) {
                // initialize firebase for OneCart, with push notification token listener
                val firebaseChatOptions = FirebaseOptions.Builder()
                    .setProjectId(getString(R.string.one_cart_chat))
                    .setApplicationId(getString(R.string.oc_chat_app_id))
                    .setApiKey(getString(R.string.oc_chat_api_key))
                    .build()

                val chatApp =
                    FirebaseApp.initializeApp(
                        this,
                        firebaseChatOptions,
                        getString(R.string.oc_chat_app),
                    )
                val fbMessaging = chatApp.get(FirebaseMessaging::class.java)
                fbMessaging.token.addOnCompleteListener { it: Task<String?> ->
                    if (it.isSuccessful) {
                        Utils.setOCChatFCMToken(it.result)
                    }
                }
            }
            // Start service to listen to incoming messages from Stream
            if (SessionUtilities.getInstance().isUserAuthenticated &&
                (!OCConstant.isOCChatBackgroundServiceRunning)
            ) {
                startOCChatService(this)
            }
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
    }



    @VisibleForTesting
    fun testsetupLoadingScreen() {
        return setupLoadingScreen()
    }

    @VisibleForTesting
    fun testSetViewModelInstance(viewModel: StartupViewModel) {
        startupViewModel = viewModel
    }

    @VisibleForTesting
    fun testSetIntent(intent: Intent) {
        deeplinkIntent = intent
    }
}
