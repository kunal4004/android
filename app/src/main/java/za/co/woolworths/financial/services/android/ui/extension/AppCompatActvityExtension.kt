@file:Suppress("UNCHECKED_CAST")

package za.co.woolworths.financial.services.android.ui.extension

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.amplifyframework.core.Amplify
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.ui.views.SafeClickListener
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

/**
 * Method to add the fragment. The [fragment] is added to the container view with id
 * [containerViewId] and a [tag]. The operation is performed by the supportFragmentManager.
 * This method checks if fragment exists.
 * @return the fragment added.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Fragment> AppCompatActivity.addFragment(
    fragment: T?,
    tag: String,
    allowStateLoss: Boolean = false,
    @IdRes containerViewId: Int,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0,
): T? {
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
fun AppCompatActivity.replaceFragmentSafely(
    fragment: Fragment,
    tag: String,
    allowStateLoss: Boolean = false,
    allowBackStack: Boolean,
    @IdRes containerViewId: Int,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0,
) {
    val ft = supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        .replace(containerViewId, fragment, tag)
    if (allowBackStack) {
        ft.addToBackStack(null)
    }
    if (!supportFragmentManager.isStateSaved) {
        ft.commit()
    } else if (allowStateLoss) {
        ft.commitAllowingStateLoss()
    }
}

fun AppCompatActivity.addFragment(
    fragment: Fragment,
    tag: String,
    allowStateLoss: Boolean = false,
    allowBackStack: Boolean,
    @IdRes containerViewId: Int,
    @AnimRes enterAnimation: Int = 0,
    @AnimRes exitAnimation: Int = 0,
    @AnimRes popEnterAnimation: Int = 0,
    @AnimRes popExitAnimation: Int = 0,
) {
    val ft = supportFragmentManager
        .beginTransaction()
        .setCustomAnimations(enterAnimation, exitAnimation, popEnterAnimation, popExitAnimation)
        .add(containerViewId, fragment, tag)
    if (allowBackStack) {
        ft.addToBackStack(null)
    }
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
            inputManager?.hideSoftInputFromWindow(
                currentFocusedView.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS,
            )
        }
    }
}

inline fun <reified T> Gson.fromJson(json: String): T =
    this.fromJson<T>(json, object : TypeToken<T>() {}.type)

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

fun EditText.afterTypingStateChanged(
    millisInFuture: Long,
    countDownInterval: Long = 10000,
    afterTypingStateChanged: (Boolean) -> Unit,
) {
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

inline fun <reified RESPONSE_OBJECT> request(
    call: Call<RESPONSE_OBJECT>?,
    requestListener: IGenericAPILoaderView<Any>? = null,
): Call<RESPONSE_OBJECT>? {
    val classType: Class<RESPONSE_OBJECT> = RESPONSE_OBJECT::class.java
    requestListener?.showProgress()
    call?.enqueue(
        CompletionHandler(
            object : IResponseListener<RESPONSE_OBJECT> {
                override fun onSuccess(response: RESPONSE_OBJECT?) {
                    requestListener?.hideProgress()
                    requestListener?.onSuccess(response)
                }

                override fun onFailure(error: Throwable?) {
                    requestListener?.hideProgress()
                    requestListener?.onFailure(error)
                }
            },
            classType,
        ),
    )

    return call
}

inline fun <reified RESPONSE_OBJECT> cancelRetrofitRequest(call: Call<RESPONSE_OBJECT>?) {
    call?.apply {
        if (!isCanceled) {
            cancel()
        }
    }
}

// Find current fragments in navigation graph
fun Fragment.getFragmentNavController(@IdRes id: Int) = activity?.let {
    return@let Navigation.findNavController(it, id)
}

@Suppress("UNCHECKED_CAST")
fun <F : Fragment> AppCompatActivity.getFragment(fragmentClass: Class<F>): F? {
    val navHostFragment = this.supportFragmentManager.fragments.first() as NavHostFragment

    navHostFragment.childFragmentManager.fragments.forEach {
        if (fragmentClass.isAssignableFrom(it.javaClass)) {
            return it as F
        }
    }

    return null
}

inline fun <reified T : Enum<T>> String.asEnumOrDefault(defaultValue: T? = null): T? =
    enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: defaultValue

fun String.isEmailValid(): Boolean {
    return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun navOptions() = NavOptions.Builder().setEnterAnim(R.anim.slide_in_from_right)
    .setExitAnim(R.anim.slide_out_to_left)
    .setPopEnterAnim(R.anim.slide_from_left)
    .setPopExitAnim(R.anim.slide_to_right).build()

fun GlobalScope.doAfterDelay(time: Long, code: () -> Unit) {
    launch {
        delay(time)
        launch(Dispatchers.Main) { code() }
    }
}

fun CoroutineScope.doAfterDelay(time: Long, code: () -> Unit) {
    launch {
        delay(time)
        launch { code() }
    }
}

fun Fragment.getNavigationResult(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(key)

fun Fragment.setNavigationResult(key: String = "result", result: String) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun <T> Fragment.setNavigationResult(key: String, value: T) {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(
        key,
        value,
    )
}

fun <T> Fragment.getNavigationResult(@IdRes id: Int, key: String, onResult: (result: T) -> Unit) {
    val navBackStackEntry = findNavController().getBackStackEntry(id)

    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_RESUME &&
            navBackStackEntry.savedStateHandle.contains(key)
        ) {
            val result = navBackStackEntry.savedStateHandle.get<T>(key)
            result?.let(onResult)
            navBackStackEntry.savedStateHandle.remove<T>(key)
        }
    }
    navBackStackEntry.lifecycle.addObserver(observer)

    viewLifecycleOwner.lifecycle.addObserver(
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        },
    )
}

fun RecyclerView.setDivider(@DrawableRes drawableRes: Int) {
    val divider = DividerItemDecoration(
        this.context,
        DividerItemDecoration.VERTICAL,
    )
    val drawable = ContextCompat.getDrawable(
        this.context,
        drawableRes,
    )
    drawable?.let {
        divider.setDrawable(it)
        addItemDecoration(divider)
    }
}

inline fun <reified T : Enum<T>> Intent.putEnumExtra(victim: T): Intent =
    putExtra(T::class.java.name, victim.ordinal)

inline fun <reified T : Enum<T>> Intent.getEnumExtra(): T? =
    getIntExtra(T::class.java.name, -1)
        .takeUnless { it == -1 }
        ?.let { T::class.java.enumConstants?.get(it) }

/**
 *  Access items of ViewPager2
 *  If Activity is the host, use FragmentManager or supportFragmentManager
 *  If Fragment is the host, use childFragmentManager
 */

fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}

fun ViewPager2.findFragmentAtPosition(
    fragmentManager: FragmentManager,
    position: Int,
): Fragment? {
    return fragmentManager.findFragmentByTag("f$position")
}

fun TextView.underline() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun Fragment.navigateSafelyWithNavController(directions: NavDirections) {
    val navController = findNavController()
    val destination = navController.currentDestination as? FragmentNavigator.Destination
    if (javaClass.name == destination?.className) {
        navController.navigate(directions)
    }
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

/**
 * maxLength extension function makes a filter that
 * will constrain edits not to make the length of the text
 * greater than the specified length.
 *
 * @param max
 */
fun EditText.maxLength(max: Int) {
    this.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(max))
}

fun NavController.navigateUpOrFinish(activity: AppCompatActivity?): Boolean {
    return if (navigateUp()) {
        true
    } else {
        activity?.finish()
        activity?.overridePendingTransition(0, 0)
        true
    }
}

/**
 * Amplify has prevented configuration be initiated twice, but there is still no way for
 * programmers to distinguish whether the configs has been initialed.
 * To be able to tell Amplify is already configured will be perfect! For now I've implemented
 * this extension method as a temporary workaround:
 */
fun isAWSAmplifyConfigured() = Amplify.API.plugins.isEmpty() &&
    Amplify.Auth.plugins.isEmpty()

fun View.onClick(result: (View) -> Unit) {
    AnimationUtilExtension.animateViewPushDown(this)
    setOnClickListener {
        result(it)
    }
}

inline fun <reified T : Any> T.json(): String = GsonBuilder().disableHtmlEscaping().create().toJson(this, T::class.java)

/**
 * Call up the dialer and call the [phoneNumber]
 *
 * @author Adebayo Oloyede
 * @param [phoneNumber] The Phone number to call
 * @receiver [Context] Intended to be used only in UI components like Fragments and Activities
 * @since 9.12.0
 * */
fun Context.makeCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)

    intent.data = Uri.parse("tel:${phoneNumber.trim()}")
    startActivity(intent)
}
