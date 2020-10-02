@file:Suppress("UNCHECKED_CAST")

package za.co.woolworths.financial.services.android.ui.extension


import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler

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
        fragment?.let { ft.add(containerViewId, it, tag) }
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
    requestFocus()
    activity.apply {
        requestFocus()
        isFocusableInTouchMode = true
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
        text?.apply { setSelection(length) }
    }
}

fun EditText.hideKeyboard(activity: AppCompatActivity) {
    activity.apply {
        val inputManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        // check if no view has focus:
        val currentFocusedView = currentFocus
        if (currentFocusedView != null) {
            inputManager?.hideSoftInputFromWindow(currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

}

inline fun <reified T> Gson.fromJson(json: String): T = this.fromJson<T>(json, object : TypeToken<T>() {}.type)

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun EditText.onAction(action: Int, runAction: () -> Unit) {
    this.setOnEditorActionListener { v, actionId, event ->
        return@setOnEditorActionListener when (actionId) {
            action -> {
                runAction.invoke()
                true
            }
            else -> false
        }
    }
}

fun EditText.afterTypingStateChanged(millisInFuture: Long, countDownInterval: Long = 10000, afterTypingStateChanged: (Boolean) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        var timer: CountDownTimer? = null
        var isTyping: Boolean = false

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            if (!isTyping) {
                isTyping = true
                afterTypingStateChanged.invoke(isTyping)
            }

            timer?.cancel()
            timer = object : CountDownTimer(millisInFuture, countDownInterval) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    isTyping = false
                    afterTypingStateChanged.invoke(isTyping)
                }
            }.start()
        }
    })
}

inline fun <reified RESPONSE_OBJECT> request(call: Call<RESPONSE_OBJECT>?, requestListener: IGenericAPILoaderView<Any>? = null): Call<RESPONSE_OBJECT>? {
    val classType: Class<RESPONSE_OBJECT> = RESPONSE_OBJECT::class.java
    requestListener?.showProgress()
    call?.enqueue(CompletionHandler(object : IResponseListener<RESPONSE_OBJECT> {
        override fun onSuccess(response: RESPONSE_OBJECT?) {
            requestListener?.hideProgress()
            requestListener?.onSuccess(response)
        }

        override fun onFailure(error: Throwable?) {
            requestListener?.hideProgress()
            requestListener?.onFailure(error)
        }
    }, classType))

    return call
}

inline fun <reified RESPONSE_OBJECT> cancelRetrofitRequest(call: Call<RESPONSE_OBJECT>?) {
    call?.apply {
        if (!isCanceled) {
            cancel()
        }
    }
}

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun Any.navOptions() = NavOptions.Builder().setEnterAnim(R.anim.slide_in_from_right)
        .setExitAnim(R.anim.slide_out_to_left)
        .setPopEnterAnim(R.anim.slide_from_left)
        .setPopExitAnim(R.anim.slide_to_right).build()


fun GlobalScope.doAfterDelay(time: Long, code: () -> Unit) {
    launch {
        delay(time)
        launch(Dispatchers.Main) { code() }
    }
}