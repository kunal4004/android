package za.co.woolworths.financial.services.android.ui.activities.onboarding

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.on_boarding_activity.*
import za.co.woolworths.financial.services.android.contracts.IViewPagerSwipeListener
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType

class OnBoardingActivity : AppCompatActivity(), IViewPagerSwipeListener, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.on_boarding_activity)
        KotlinUtils.setTransparentStatusBar(this)

        val onBoardingNavigationGraph = findNavController(R.id.on_boarding_navigation_graph)
        KotlinUtils.setAccountNavigationGraph(onBoardingNavigationGraph, OnBoardingScreenType.START_UP)

        AnimationUtilExtension.animateViewPushDown(signInButton)
        AnimationUtilExtension.animateViewPushDown(registerButton)
        AnimationUtilExtension.animateViewPushDown(letsGoButton)
        AnimationUtilExtension.animateViewPushDown(skipButton)

        skipButton?.paintFlags = skipButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        skipButton?.setOnClickListener(this)
        signInButton?.setOnClickListener(this)
        letsGoButton?.setOnClickListener(this)
        registerButton?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.skipButton, R.id.letsGoButton -> navigateToBottomNavigationActivity()
            R.id.registerButton -> ScreenManager.presentSSORegister(this@OnBoardingActivity)
            R.id.signInButton -> ScreenManager.presentSSOSignin(this@OnBoardingActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                navigateToBottomNavigationActivity()
            }
        }
    }

    private fun navigateToBottomNavigationActivity() {
        Utils.sessionDaoSave(SessionDao.KEY.ON_BOARDING_SCREEN, "1")
        val bottomNavigationActivityIntent = Intent(this, BottomNavigationActivity::class.java)
        startActivityForResult(bottomNavigationActivityIntent, 0)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.fade_out)
    }

    override fun onPagerSwipe(position: Int, listSize: Int) {
        when (position) {
            (listSize - 1) -> {
                skipButton?.visibility = View.GONE
                letsGoButton?.visibility = View.VISIBLE
            }
            else -> {
                skipButton?.visibility = View.VISIBLE
                letsGoButton?.visibility = View.GONE
            }
        }
    }
}