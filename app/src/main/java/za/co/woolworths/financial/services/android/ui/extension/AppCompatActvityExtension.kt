package za.co.woolworths.financial.services.android.ui.extension

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.ContextWrapper
import android.support.annotation.AnimRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.EditText
import android.view.WindowManager
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.INPUT_METHOD_SERVICE





/**
 * Method to add the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 * This method checks if fragment exists.
 * @return the fragment added.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Fragment> AppCompatActivity.addFragment(fragment: T?,
                                                 tag: String,
                                                 allowStateLoss: Boolean = false,
                                                 @IdRes containerViewId: Int,
                                                 @AnimRes enterAnimation: Int = 0,
                                                 @AnimRes exitAnimation: Int = 0,
                                                 @AnimRes popEnterAnimation: Int = 0,
                                                 @AnimRes popExitAnimation: Int = 0): T? {
    if (!existsFragmentByTag(tag)) {
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        ft.add(containerViewId, fragment, tag)
        if (!supportFragmentManager.isStateSaved) {
            ft.commit()
        } else if (allowStateLoss) {
            ft.commitAllowingStateLoss()
        }
        return fragment
    }
    return findFragmentByTag(tag) as T
}

/**
 * Method to replace the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.replaceFragmentSafely(fragment: Fragment,
                                            tag: String,
                                            allowStateLoss: Boolean = false,
                                            allowBackStack: Boolean,
                                            @IdRes containerViewId: Int,
                                            @AnimRes enterAnimation: Int = 0,
                                            @AnimRes exitAnimation: Int = 0,
                                            @AnimRes popEnterAnimation: Int = 0,
                                            @AnimRes popExitAnimation: Int = 0) {
    val ft = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
            .replace(containerViewId, fragment, tag)
    if (allowBackStack)
        ft.addToBackStack(null)
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}

fun AppCompatActivity.addFragment(fragment: Fragment,
                                  tag: String,
                                  allowStateLoss: Boolean = false,
                                  allowBackStack: Boolean,
                                  @IdRes containerViewId: Int,
                                  @AnimRes enterAnimation: Int = 0,
                                  @AnimRes exitAnimation: Int = 0,
                                  @AnimRes popEnterAnimation: Int = 0,
                                  @AnimRes popExitAnimation: Int = 0) {
    val ft = supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
            .add(containerViewId, fragment, tag)
    if (allowBackStack)
        ft.addToBackStack(null)
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}

/**
 * Method to check if fragment exists. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.existsFragmentByTag(tag: String): Boolean {
    return supportFragmentManager.findFragmentByTag(tag) != null
}

/**
 * Method to get fragment by tag. The operation is performed by the supportFragmentManager.
 */
fun AppCompatActivity.findFragmentByTag(tag: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(tag)
}

fun EditText.showKeyboard(activity: AppCompatActivity) {
    if (requestFocus()) {
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.toggleSoftInputFromWindow(
                windowToken,
                InputMethodManager.SHOW_FORCED, 0)
        text?.apply { setSelection(length) }
    }
}

fun EditText.hideKeyboard() {
    val inputManager = context.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
    inputManager?.hideSoftInputFromWindow(getActivity()?.window?.decorView?.applicationWindowToken, 0)
    getActivity()?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
}

fun View.getActivity(): AppCompatActivity? {
    var context = this.context
    while (context is ContextWrapper) {
        if (context is AppCompatActivity) {
            return context
        }
        context = context.baseContext
    }
    return null
}