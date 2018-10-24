package za.co.woolworths.financial.services.android.ui.extension

import android.os.Bundle
import android.support.annotation.AnimRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment

/**
 * Method to replace the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 */
fun Fragment.replaceFragment(fragment: Fragment,
                             tag: String,
                             allowStateLoss: Boolean = false,
                             @IdRes containerViewId: Int,
                             @AnimRes enterAnimation: Int = 0,
                             @AnimRes exitAnimation: Int = 0,
                             @AnimRes popEnterAnimation: Int = 0,
                             @AnimRes popExitAnimation: Int = 0) {
    if (activity != null) {
        val ft = activity.supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
                .addToBackStack(fragment.javaClass.name)
                .replace(containerViewId, fragment, tag)
        if (!activity.supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
    }
}

inline fun <T : Fragment> T.withArgs(
        argsBuilder: Bundle.() -> Unit): T =
        this.apply {
            arguments = Bundle().apply(argsBuilder)
        }