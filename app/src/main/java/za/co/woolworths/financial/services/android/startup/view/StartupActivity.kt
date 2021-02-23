package za.co.woolworths.financial.services.android.startup.view

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.ViewModelProviders
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.internal.common.CommonUtils
import kotlinx.android.synthetic.main.activity_startup.*
import kotlinx.android.synthetic.main.activity_startup_with_message.*
import kotlinx.android.synthetic.main.activity_startup_without_video.*
import com.awfs.coordination.BuildConfig
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.Resource
import za.co.woolworths.financial.services.android.service.network.ResponseStatus
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel.Companion.APP_SERVER_ENVIRONMENT_KEY
import za.co.woolworths.financial.services.android.startup.viewmodel.StartupViewModel.Companion.APP_VERSION_KEY
import za.co.woolworths.financial.services.android.startup.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.util.*
import java.util.*

class StartupActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, View.OnClickListener {

    private lateinit var startupViewModel: StartupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        setSupportActionBar(mToolbar)
        setupViewModel()
        window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createNotificationChannelIfNeeded(this);
        };

        progressBar?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)

        // Disable first time launch splash video screen, remove to enable video on startup
        Utils.sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1")

        startupViewModel?.apply {
            this.intent = getIntent()
            val bundle = getIntent()?.extras

            try {
                appVersion = packageManager.getPackageInfo(packageName, 0).versionName
                environment = BuildConfig.ENV
            } catch (e: PackageManager.NameNotFoundException) {
                appVersion = "6.1.0"
                environment = "QA"
            }

            firebaseAnalytics = FirebaseAnalytics.getInstance(this@StartupActivity)

            if (NetworkManager.getInstance().isConnectedToNetwork(this@StartupActivity)) {
                firebaseAnalytics?.apply {
                    setUserProperty(APP_SERVER_ENVIRONMENT_KEY, if (environment?.isEmpty() == true) "prod" else environment?.toLowerCase(Locale.getDefault()))
                    setUserProperty(APP_VERSION_KEY, appVersion)

                    val token = SessionUtilities.getInstance().jwt
                    token.AtgId?.apply {
                        val atgId = if (this.isJsonArray) this.asJsonArray.first().asString else this.asString
                        setUserId(atgId)
                        setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.ATGId, atgId)
                    }

                    token.C2Id?.apply {
                        setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, this)
                    }
                }
                setupScreen()
            } else {
                showNonVideoViewWithErrorLayout()
            }

            retry?.setOnClickListener(this@StartupActivity)

            //Remove old usage of SharedPreferences data.
            Utils.clearSharedPreferences(this@StartupActivity)
            AuthenticateUtils.getInstance(this@StartupActivity).enableBiometricForCurrentSession(true)
        }
    }

    private fun setupScreen() {
        val isFirstTime: Boolean = isFirstTime()
        if (isFirstTime) {
            showVideoView()
        } else {
            showNonVideoViewWithoutErrorLayout()
        }
    }

    private fun isFirstTime(): Boolean {
        return Utils.getSessionDaoValue(SessionDao.KEY.SPLASH_VIDEO) == null
    }

    private fun showVideoView() {
        splashNoVideoView?.visibility = View.GONE
        splashServerMessageView?.visibility = View.GONE
        videoViewLayout?.visibility = View.VISIBLE

        val randomVideo = startupViewModel?.randomVideoPath
        if (randomVideo?.isNotEmpty() == true) {
            val videoUri = Uri.parse(randomVideo)
            activity_wsplash_screen_videoview?.apply {
                setVideoURI(videoUri)
                start()
                setOnCompletionListener(this@StartupActivity)
            }
            startupViewModel?.isVideoPlaying = false
        }
    }

    private fun showNonVideoViewWithErrorLayout() {
        runOnUiThread {
            progressBar?.visibility = View.GONE
            splashNoVideoView?.visibility = View.GONE
            splashNoVideoView?.visibility = View.VISIBLE
            splashServerMessageView?.visibility = View.GONE
            errorLayout?.visibility = View.VISIBLE
        }
    }

    private fun showNonVideoViewWithoutErrorLayout() {
        progressBar?.visibility = View.VISIBLE
        videoViewLayout?.visibility = View.GONE
        errorLayout?.visibility = View.GONE
        splashNoVideoView?.visibility = View.VISIBLE
        splashServerMessageView?.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        startupViewModel?.apply {
            when (v?.id) {
                R.id.retry -> {
                    if (NetworkManager.getInstance().isConnectedToNetwork(this@StartupActivity)) {
                        firebaseAnalytics?.apply {
                            setUserProperty(APP_SERVER_ENVIRONMENT_KEY, if (environment?.isEmpty() == true) "prod" else environment?.toLowerCase(Locale.getDefault()))
                            setUserProperty(APP_VERSION_KEY, appVersion)
                        }
                        setupScreen()
                        getConfig()
                    } else {
                        showNonVideoViewWithErrorLayout()
                    }
                }
            }
        }
    }

    private fun getConfig() {
        startupViewModel.queryServiceGetConfig().observe(this, {
            when (it.responseStatus) {
                ResponseStatus.SUCCESS -> {
                    Resource.persistGlobalConfig(it.data, startupViewModel)
                    startupViewModel.videoPlayerShouldPlay = false
                    if (TextUtils.isEmpty(it.data?.configs?.enviroment?.stsURI)) {
                        showNonVideoViewWithErrorLayout()
                        return@observe
                    }
                    if (!startupViewModel.isVideoPlaying) {
                        presentNextScreenOrServerMessage()
                    }

                }
                ResponseStatus.LOADING -> {

                }
                ResponseStatus.ERROR -> {
                    showNonVideoViewWithErrorLayout()
                }
            }
        })
    }

    //video player on completion
    override fun onCompletion(mp: MediaPlayer?) {
        startupViewModel?.apply {
            isVideoPlaying = false
            if (!videoPlayerShouldPlay) {
                presentNextScreenOrServerMessage()
                mp?.stop()
            } else {
                showNonVideoViewWithoutErrorLayout()
            }
        }
    }

    private fun presentNextScreenOrServerMessage() {
        if (startupViewModel?.isSplashScreenDisplay == true) {
            showServerMessage()
        } else {
            showNonVideoViewWithoutErrorLayout()
            presentNextScreen()
        }
    }

    private fun showServerMessage() {
        progressBar?.visibility = View.GONE
        videoViewLayout?.visibility = View.GONE
        errorLayout?.visibility = View.GONE
        splashNoVideoView?.visibility = View.GONE
        messageLabel?.setText(startupViewModel?.splashScreenText)
        if (startupViewModel?.isSplashScreenPersist == true) {
            proceedButton?.visibility = View.GONE
        } else {
            proceedButton?.visibility = View.VISIBLE
            proceedButton?.setOnClickListener { _: View? ->
                showNonVideoViewWithoutErrorLayout()
                presentNextScreen()
            }
        }
        splashServerMessageView?.visibility = View.VISIBLE
        startupViewModel?.isServerMessageShown = true
    }

    fun setupViewModel() {
        startupViewModel = ViewModelProviders.of(
                this,
                ViewModelFactory(StartUpRepository(StartupApiHelper()))
        ).get(StartupViewModel::class.java)
    }

    fun presentNextScreen() {

        val isFirstTime = Utils.getSessionDaoValue(SessionDao.KEY.ON_BOARDING_SCREEN)
        var appLinkData: Any? = intent?.data

        if (appLinkData == null && intent?.extras != null) {
            appLinkData = intent!!.extras!!
            intent?.action = Intent.ACTION_VIEW
        }

        if (Intent.ACTION_VIEW == intent?.action && appLinkData != null) {
            handleAppLink(appLinkData)
        } else {
            val activity = this as Activity
            if (isFirstTime == null || Utils.isAppUpdated(this))
                ScreenManager.presentOnboarding(activity)
            else {
                ScreenManager.presentMain(activity)
            }
        }
    }

    private fun handleAppLink(appLinkData: Any?) {
        // val productSearchViewModel: ProductSearchViewModel = ProductSearchViewModelImpl();
        //productSearchViewModel.getTypeAndTerm(urlString = appLinkData.toString())
        //1. check URL
        //2. navigate to facet that URL corresponds to
        ScreenManager.presentMain(this as Activity, appLinkData as Bundle)
    }

    override fun onStart() {
        super.onStart()
        if (Utils.checkForBinarySu() && CommonUtils.isRooted(this) && !Util.isDebug(WoolworthsApplication.getAppContext())) {
            Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_ROOTED_AT_STARTUP)
            val rootedDeviceInfoFragment = newInstance(getString(R.string.rooted_phone_desc))
            rootedDeviceInfoFragment.show(supportFragmentManager, RootedDeviceInfoFragment::class.java.simpleName)
            return
        }
        startupViewModel?.apply {
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
                getConfig()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        startupViewModel?.isAppMinimized = true
    }

    override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STARTUP)
        NotificationUtils.clearNotifications(this@StartupActivity)
    }

    @VisibleForTesting
    fun testIsFirstTime(): Boolean {
        return isFirstTime()
    }

    @VisibleForTesting
    fun testGetRandomVideos(): String? {
        return startupViewModel?.randomVideoPath
    }
}