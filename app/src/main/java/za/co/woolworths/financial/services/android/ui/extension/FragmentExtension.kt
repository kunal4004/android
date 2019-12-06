package za.co.woolworths.financial.services.android.ui.extension

import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.NetworkManager

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

    activity?.apply {
        val ft = supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
                .addToBackStack(fragment.javaClass.name)
                .replace(containerViewId, fragment, tag)
        if (!supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
    }

}

fun Fragment.replaceChildFragmentSafely(fragment: Fragment,
                                   tag: String,
                                   allowStateLoss: Boolean = false,
                                   allowBackStack: Boolean,
                                   @IdRes containerViewId: Int,
                                   @AnimRes enterAnimation: Int = 0,
                                   @AnimRes exitAnimation: Int = 0,
                                   @AnimRes popEnterAnimation: Int = 0,
                                   @AnimRes popExitAnimation: Int = 0) {

    activity?.apply {
        val ft = childFragmentManager
                .beginTransaction()
                .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
                .replace(containerViewId, fragment, tag)
        if (allowBackStack)
            ft.addToBackStack(null)
        if (!childFragmentManager.isStateSaved) {
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

fun isConnectedToNetwork(): Boolean? = NetworkManager.getInstance()?.isConnectedToNetwork(WoolworthsApplication.getAppContext())
        ?: false
