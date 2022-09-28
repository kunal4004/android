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
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.activity_startup.*
import kotlinx.android.synthetic.main.activity_startup_without_video.*
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.firebase.FirebaseConfigUtils
import za.co.woolworths.financial.services.android.firebase.model.ConfigData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyDetails
import za.co.woolworths.financial.services.android.models.dto.voc.SurveyQuestion
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.startup.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.voc.VoiceOfCustomerManager
import javax.inject.Inject

@AndroidEntryPoint
class StartupActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener,
    View.OnClickListener {

    private lateinit var configBuilder: FirebaseRemoteConfigSettings.Builder
    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var startupViewModel: StartupViewModel
    private lateinit var deeplinkIntent: Intent
    private var actionUrlFirst: String? = AppConstant.EMPTY_STRING
    private var actionUrlSecond: String? = AppConstant.EMPTY_STRING
    private var remoteConfigJsonString: String = AppConstant.EMPTY_STRING
    private var isAppSideLoaded = false

    @Inject lateinit var notificationUtils: NotificationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setUpFirebaseconfig()

        try {
            // Try to get a drawable, to make sure the app has not
            // been sideloaded to a device with a different pixel density
            AppCompatResources.getDrawable(this, R.drawable.splash_w_logo)

            setSupportActionBar(mToolbar)
            setContentView(R.layout.activity_startup)

            window?.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            if (supportActionBar?.isShowing == true)
                supportActionBar?.hide()
            progressBar?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
            retry?.setOnClickListener(this@StartupActivity)
            deeplinkIntent = intent
            init()
        } catch (e: Resources.NotFoundException) {
            // Consider the app has been sideloaded and the split APK doesn't have
            // the required drawables to run on this specific device
            setContentView(R.layout.activity_startup_resourcenotfound)
            isAppSideLoaded = true
        }
    }

    private fun setUpFirebaseconfig() {
        firebaseRemoteConfig = startupViewModel.getFirebaseRemoteConfigData();
         configBuilder = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(AppConstant.FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL)
                .setFetchTimeoutInSeconds(AppConstant.FIREBASE_REMOTE_CONFIG_TIMEOUT_INTERVAL)
        val defaultJsonString = FirebaseConfigUtils.getJsonDataFromAsset(this, FirebaseConfigUtils.FILE_NAME)
        val defaultValues = mutableMapOf( FirebaseConfigUtils.CONFIG_KEY to defaultJsonString)
        firebaseRemoteConfig.setConfigSettingsAsync(configBuilder.build())
        firebaseRemoteConfig.setDefaultsAsync(defaultValues as Map<String, Any>)
    }

    private fun fetchFirebaseConfigData(isComingFromSuccess:Boolean) {
        firebaseRemoteConfig
                .fetch(AppConstant.FIREBASE_REMOTE_CONFIG_FETCH_INTERVAL).addOnCompleteListener { task ->
            run {
                if (task.isSuccessful) {
                    //set dynamic ui here
                    firebaseRemoteConfig.activate()
                    remoteConfigJsonString = startupViewModel.fetchFirebaseRemoteConifgData()

                    if (isComingFromSuccess) {
                         //success of api
                        if (remoteConfigJsonString.isEmpty()) {
                            // api successfull but firebase not configured so navigate with normal flow
                            presentNextScreenOrServerMessage()
                        } else {
                            // api successfull and  firebase also configured so display sunsetting ui
                            setContentView(R.layout.activity_splash_screen)
                            val configData:ConfigData? = startupViewModel.parseRemoteconfigData(remoteConfigJsonString)
                            if (configData?.expiryTime == -1L || configData == null) {
                                // in case we get json exception while parsing then we navigate with normal flow
                                progress_bar?.visibility = View.GONE
                                presentNextScreenOrServerMessage()
                            } else {
                                setDataOnUI(configData, true)
                            }
                        }
                    } else {
                        // error  of api
                        if (remoteConfigJsonString.isEmpty()) {
                            //api is  failed and firebase not configured so show error screen of api reposne
                            showNonVideoViewWithErrorLayout()
                        } else {
                             // api is failed and sunsetting is cofigured then show sunsetting ui

                            val configData:ConfigData? = startupViewModel.parseRemoteconfigData(remoteConfigJsonString)
                            if (configData?.expiryTime == -1L || configData == null) {
                                // in case we get json exception while parsing then show error screen of api
                                progress_bar?.visibility = View.GONE
                                showNonVideoViewWithErrorLayout()
                            } else {
                                setContentView(R.layout.activity_splash_screen)
                                setDataOnUI(configData, false)
                            }
                        }
                    }
                } else {
                    // firebase fail
                    if (isComingFromSuccess) {
                        // api is success and firebase  is failed so navigate to next screen
                        progress_bar?.visibility = View.GONE
                        presentNextScreenOrServerMessage()
                    } else  {
                        // api is failed and firebase  is failed so display error layout

                        progress_bar?.visibility = View.GONE
                        showNonVideoViewWithErrorLayout()
                    }
                }
            }
        }
    }

    private fun setDataOnUI(configData: ConfigData?, isComingFromSuccess: Boolean) {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.SPLASH_WITH_CTA)
        progress_bar?.visibility = View.GONE
        first_btn?.visibility = View.VISIBLE
        second_btn?.visibility = View.VISIBLE
        first_btn?.setOnClickListener(this)
        second_btn?.setOnClickListener(this)

        val timeIntervalSince1970: Long = System.currentTimeMillis()

        if (configData != null) {
            if (timeIntervalSince1970 < configData.expiryTime) {
                val activeConfiguration = configData.activeConfiguration
                activeConfiguration?.run {
                    if (title == null)
                        txt_title?.visibility = View.GONE
                    else
                        txt_title?.text = activeConfiguration.title

                    if (description == null)
                        txt_desc?.visibility = View.GONE
                    else
                        txt_desc?.text = activeConfiguration.description

                    if (imageUrl == null)
                        img_view?.visibility = View.GONE
                    else {
                        if (imageUrl.isEmpty())
                            img_view.setImageResource(R.drawable.link_icon)
                        else
                            ImageManager.setPictureWithSplashPlaceHolder(img_view, imageUrl)
                    }

                    if (firstButton == null)
                        first_btn?.visibility = View.GONE
                    else {
                        first_btn?.text = firstButton.title
                        actionUrlFirst = firstButton.actionUrl
                    }

                    if (secondButton == null)
                        second_btn?.visibility = View.GONE
                    else {
                        second_btn?.text = secondButton.title
                        actionUrlSecond = secondButton.actionUrl
                    }
                }
            } else if (timeIntervalSince1970 >= configData.expiryTime && timeIntervalSince1970 != -1L) {
                val inActiveConfiguration = configData?.inactiveConfiguration
                inActiveConfiguration?.run {
                    if (title == null)
                        txt_title?.visibility = View.GONE
                    else
                        txt_title?.text = inActiveConfiguration.title

                    if (description == null)
                        txt_desc?.visibility = View.GONE
                    else
                        txt_desc?.text = inActiveConfiguration.description

                    if (imageUrl == null)
                        img_view?.visibility = View.GONE
                    else {
                        if(imageUrl.isEmpty())
                            img_view.setImageResource(R.drawable.link_icon)
                        else
                            ImageManager.setPictureWithSplashPlaceHolder(img_view, imageUrl)
                    }

                    if (firstButton == null)
                        first_btn?.visibility = View.GONE
                    else {
                        first_btn?.text = firstButton.title
                        actionUrlFirst = firstButton.actionUrl
                    }

                    if (secondButton == null)
                        second_btn?.visibility = View.GONE
                    else {
                        second_btn?.text = secondButton.title
                        actionUrlSecond = secondButton.actionUrl
                    }
                }
            } else if(configData.expiryTime == -1L && isComingFromSuccess) {
                presentNextScreenOrServerMessage()
            } else if (configData.expiryTime == -1L && !isComingFromSuccess) {
                showNonVideoViewWithErrorLayout()
            }
        }
    }

    fun init() {
        //TODO:: Handle notification for Android R
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationUtils.createNotificationChannelIfNeeded()
        }
        // Disable first time launch splash video screen, remove to enable video on startup
        startupViewModel.setSessionDao(SessionDao.KEY.SPLASH_VIDEO, "1")
        startupViewModel.setUpEnvironment(this@StartupActivity)
        if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
            startupViewModel.setUpFirebaseEvents()
        } else {
            showNonVideoViewWithErrorLayout()
        }
        //Remove old usage of SharedPreferences data.
     //   startupViewModel.clearSharedPreference(this@StartupActivity)
        AuthenticateUtils.getInstance(this@StartupActivity).enableBiometricForCurrentSession(true)
    }

    private fun setupLoadingScreen() {
        if (isFirstTime()) {
            showVideoView()
        } else {
            showNonVideoViewWithoutErrorLayout()
        }
    }

    private fun isFirstTime(): Boolean {
        return startupViewModel.getSessionDao(SessionDao.KEY.SPLASH_VIDEO)
    }

    fun showVideoView() {
        splashNoVideoView?.visibility = View.GONE
        splashServerMessageView?.visibility = View.GONE
        videoViewLayout?.visibility = View.VISIBLE

        val randomVideo = startupViewModel.randomVideoPath
        if (randomVideo.isNotEmpty()) {
            val videoUri = Uri.parse(randomVideo)
            activity_wsplash_screen_videoview?.apply {
                setVideoURI(videoUri)
                start()
                setOnCompletionListener(this@StartupActivity)
            }
            startupViewModel.isVideoPlaying = false
        }
    }

    fun showNonVideoViewWithErrorLayout() {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.STARTUP_API_ERROR)
        runOnUiThread {
            progressBar?.visibility = View.GONE
            splashNoVideoView?.visibility = View.GONE
            splashNoVideoView?.visibility = View.VISIBLE
            splashServerMessageView?.visibility = View.GONE
            errorLayout?.visibility = View.VISIBLE
        }
    }

    fun showNonVideoViewWithoutErrorLayout() {
        progressBar?.visibility = View.VISIBLE
        videoViewLayout?.visibility = View.GONE
        errorLayout?.visibility = View.GONE
        splashNoVideoView?.visibility = View.VISIBLE
        splashServerMessageView?.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        startupViewModel.apply {
            when (v?.id) {
                R.id.retry -> {
                    if (startupViewModel.isConnectedToInternet(this@StartupActivity)) {
                        startupViewModel.setupFirebaseUserProperty()
                        getConfig()
                    } else {
                        showNonVideoViewWithErrorLayout()
                    }
                }
                R.id.first_btn-> handleFirstbuttonClick()
                R.id.second_btn-> handleSecondbuttonClick()
            }
        }
    }

    private fun handleSecondbuttonClick() {
        val text: String = second_btn?.text.toString()
        val updatedText: String = Utils.formatAnalyticsButtonText(text)
        if (!text.isEmpty()) {
            Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SPLASH_BTN.plus(updatedText),
                    this
            )
        }
        if (actionUrlSecond.isNullOrEmpty()) {
            presentNextScreen()
        } else {
            ScreenManager.presentToActionView(this, actionUrlSecond)
        }
    }

    private fun handleFirstbuttonClick() {
        val text: String = first_btn?.text.toString()
        val updatedText: String = Utils.formatAnalyticsButtonText(text)
        if (!text.isEmpty()) {
            Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SPLASH_BTN.plus(updatedText) ,
                    this
            )
        }
        if (actionUrlFirst.isNullOrEmpty()) {
            presentNextScreen()
        }  else {
            ScreenManager.presentToActionView(this, actionUrlFirst)
        }
    }

    fun getConfig() {
        startupViewModel.queryServiceGetConfig().observe(this) {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    ConfigResource.persistGlobalConfig(it.data, startupViewModel)
                    startupViewModel.videoPlayerShouldPlay = false
                    if (TextUtils.isEmpty(it.data?.configs?.enviroment?.stsURI)) {
                        showNonVideoViewWithErrorLayout()
                        return@observe
                    }

                    if (!startupViewModel.isVideoPlaying) {
                        if (startupViewModel.isConnectedToInternet(this)) {
                            fetchFirebaseConfigData(true)
                        } else {
                            showNonVideoViewWithErrorLayout()
                        }
                    }
                }
                ResponseStatus.LOADING -> {
                    setupLoadingScreen()
                }
                ResponseStatus.ERROR -> {
                    fetchFirebaseConfigData(false)
                }
            }
        }
    }

    //video player on completion
    override fun onCompletion(mp: MediaPlayer?) {
        startupViewModel.apply {
            isVideoPlaying = false
            if (!videoPlayerShouldPlay) {
                presentNextScreenOrServerMessage()
                mp?.stop()
            } else {
                showNonVideoViewWithoutErrorLayout()
            }
        }
    }

    fun presentNextScreenOrServerMessage() {
        Utils.setScreenName(FirebaseManagerAnalyticsProperties.ScreenNames.SPLASH_WITHOUT_CTA)
        showNonVideoViewWithoutErrorLayout()
        presentNextScreen()
    }

    private fun setupViewModel() {
        startupViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(StartUpRepository(StartupApiHelper()), StartupApiHelper())
        ).get(StartupViewModel::class.java)
    }

    fun presentNextScreen() {
        val isFirstTime = startupViewModel.getSessionDao(SessionDao.KEY.ON_BOARDING_SCREEN)
        var appLinkData: Any? = deeplinkIntent.data

        AppConfigSingleton.isBadgesRequired = deeplinkIntent.extras?.containsKey("google.message_id") != true

        if (appLinkData == null && deeplinkIntent.extras != null) {
            appLinkData = deeplinkIntent.extras
            deeplinkIntent.action = Intent.ACTION_VIEW
        }

        if (Intent.ACTION_VIEW == deeplinkIntent.action && appLinkData != null) {
            handleAppLink(appLinkData)
        } else {
            val activity = this as Activity
            if (isFirstTime == null || Utils.isAppUpdated(this))
                ScreenManager.presentOnboarding(activity)
            else {
                ScreenManager.presentMain(activity)
            }
        }
        // forgot password deeplink
        forgotPasswordDeeplink()
    }

    fun handleAppLink(appLinkData: Any?) {
        // val productSearchViewModel: ProductSearchViewModel = ProductSearchViewModelImpl();
        //productSearchViewModel.getTypeAndTerm(urlString = appLinkData.toString())
        //1. check URL
        //2. navigate to facet that URL corresponds to
        if (appLinkData is Uri) {
            val bundle = bundleOf(
                "feature" to AppConstant.DP_LINKING_PRODUCT_LISTING,
                "parameters" to "{\"url\": \"${appLinkData}\"}"
            )
            ScreenManager.presentMain(this@StartupActivity, bundle)
        } else {
            ScreenManager.presentMain(this@StartupActivity, appLinkData as Bundle)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isAppSideLoaded) {
            Utils.setScreenName(
                FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_SIDELOADED_AT_STARTUP
            )
        } else {
            if (Utils.checkForBinarySu() && CommonUtils.isRooted(this) && !Util.isDebug(
                    this.applicationContext
                )
            ) {
                Utils.setScreenName(
                    FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_ROOTED_AT_STARTUP
                )
                val rootedDeviceInfoFragment = newInstance(getString(R.string.rooted_phone_desc))
                rootedDeviceInfoFragment.show(
                    supportFragmentManager,
                    RootedDeviceInfoFragment::class.java.simpleName
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
                    showNonVideoViewWithoutErrorLayout()
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


        // TODO: Testing VOC, to be removed
        var questions = ArrayList<SurveyQuestion>()
        questions.add(
            SurveyQuestion(
                id = 1,
                type = "NUMERIC",
                title = "This is a rate slider survey question.",
                required = true,
                minValue = 1,
                maxValue = 11
            )
        )
        questions.add(
            SurveyQuestion(
                id = 2,
                type = "FREE_TEXT",
                title = "This is a free text survey question.",
                required = true
            )
        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
//        questions.add(
//            SurveyQuestion(
//                id = 2,
//                type = "FREE_TEXT",
//                title = "This is a free text survey question.",
//                required = true
//            )
//        )
        var survey = SurveyDetails(
            id = 1,
            name = "Test",
            type = "dummy",
            questions = questions
        )
        VoiceOfCustomerManager.showVocSurvey(this, survey)
    }

    private fun forgotPasswordDeeplink() {
        var uri = intent.data
        if (null != uri) {
            var params = uri.pathSegments
            var forgotPassword = params[params.size - 1]
            if (null != forgotPassword && forgotPassword.contentEquals("forgot-password")) {
                getForgotPasswordLink(uri.toString())
            }
        }
    }

    private fun getForgotPasswordLink(forgotPasswordUri: String) {
        ScreenManager.forgotPassword(this@StartupActivity,forgotPasswordUri)
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