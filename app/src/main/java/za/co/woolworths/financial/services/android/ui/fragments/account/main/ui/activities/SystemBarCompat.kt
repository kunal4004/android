package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities

import android.app.Activity
import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import javax.inject.Inject

interface ISystemBarCompat {
    fun setLightStatusAndNavigationBar()
    fun setDarkStatusAndNavigationBar()
    fun setStatusBarInset(view: View)
    fun setStatusBarVisibility(isVisible: Boolean)
}

class SystemBarCompat @Inject constructor(private val activity: Activity) : ISystemBarCompat {

    override fun setLightStatusAndNavigationBar() {
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        windowInsetsController?.isAppearanceLightNavigationBars = false
        windowInsetsController?.isAppearanceLightStatusBars = false
        (activity as? AppCompatActivity)?.window?.statusBarColor = Color.TRANSPARENT
    }

    override fun setDarkStatusAndNavigationBar() {
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        windowInsetsController?.isAppearanceLightNavigationBars = true
        windowInsetsController?.isAppearanceLightStatusBars = true
        (activity as? AppCompatActivity)?.window?.statusBarColor = Color.WHITE
    }

    override fun setStatusBarInset(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
            // Apply the insets as padding to the view. Here we're setting all of the
            // dimensions, but apply as appropriate to your layout. You could also
            // update the views margin if more appropriate.
            view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)

            // Return CONSUMED if we don't want the window insets to keep being passed
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun setStatusBarVisibility(isVisible: Boolean) {
        // Hide the system bars.
        val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
        when (isVisible) {
            true -> windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            false -> windowInsetsController?.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

}