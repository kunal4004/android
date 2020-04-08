package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.services.common.CommonUtils
import kotlinx.android.synthetic.main.activity_startup.*
import kotlinx.android.synthetic.main.activity_startup_with_message.*
import kotlinx.android.synthetic.main.activity_startup_without_video.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment.Companion.newInstance
import za.co.woolworths.financial.services.android.util.AuthenticateUtils
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.NotificationUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModel
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModelImpl
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModelImpl.Companion.APP_SERVER_ENVIRONMENT_KEY
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModelImpl.Companion.APP_VERSION_KEY
import java.util.*

class StartupActivity : AppCompatActivity(), MediaPlayer.OnCompletionListener, View.OnClickListener {

    private var startupViewModel: StartupViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
        setSupportActionBar(mToolbar)
        window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        supportActionBar?.hide()

        progressBar?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)

        // Disable first time launch splash video screen, remove to enable video on startup
        Utils.sessionDaoSave(SessionDao.KEY.SPLASH_VIDEO, "1")

        startupViewModel = StartupViewModelImpl(this)
        startupViewModel?.apply {
            this.intent = getIntent()
            val bundle = getIntent()?.extras

            pushNotificationUpdate = bundle?.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT)

            try {
                appVersion = packageManager.getPackageInfo(packageName, 0).versionName
                environment = BuildConfig.FLAVOR
            } catch (e: PackageManager.NameNotFoundException) {
                appVersion = "6.1.0"
                environment = "QA"
            }

            firebaseAnalytics = FirebaseAnalytics.getInstance(this@StartupActivity)

            if (NetworkManager.getInstance().isConnectedToNetwork(this@StartupActivity)) {
                firebaseAnalytics?.apply {
                    setUserProperty(APP_SERVER_ENVIRONMENT_KEY, if (environment?.isEmpty() == true) "prod" else environment?.toLowerCase(Locale.getDefault()))
                    setUserProperty(APP_VERSION_KEY, appVersion)
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
                        initialize()
                    } else {
                        showNonVideoViewWithErrorLayout()
                    }
                }
            }
        }
    }

    private fun initialize() {
        startupViewModel?.apply {
            queryServiceGetConfig(object : IResponseListener<ConfigResponse?> {
                override fun onSuccess(response: ConfigResponse?) {
                    videoPlayerShouldPlay = false
                    if (response?.configs?.enviroment?.stsURI == null || response.configs.enviroment.stsURI.isEmpty()) {
                        showNonVideoViewWithErrorLayout()
                        return
                    }
                    if (!isVideoPlaying) {
                        presentNextScreenOrServerMessage()
                    }
                }

                override fun onFailure(throwable: Throwable) {
                    showNonVideoViewWithErrorLayout()
                }
            })
        }
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
            startupViewModel?.presentNextScreen()
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
                startupViewModel?.presentNextScreen()
            }
        }
        splashServerMessageView?.visibility = View.VISIBLE
        startupViewModel?.isServerMessageShown = true
    }

    override fun onStart() {
        super.onStart()
        if (Utils.checkForBinarySu() && CommonUtils.isRooted(this)) {
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
                    initialize()
                } else {
                    startActivity(Intent(this@StartupActivity, StartupActivity::class.java))
                    finish()
                }
            } else {
                initialize()
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