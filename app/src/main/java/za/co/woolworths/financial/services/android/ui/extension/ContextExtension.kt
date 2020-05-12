package za.co.woolworths.financial.services.android.ui.extension

import android.app.Activity
import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

fun Activity.bindDrawable(@DrawableRes drawableResource: Int) = ContextCompat.getDrawable(WoolworthsApplication.getAppContext(), drawableResource)

fun Fragment.bindDrawable(@DrawableRes drawableResource: Int) = ContextCompat.getDrawable(WoolworthsApplication.getAppContext(), drawableResource)

fun Activity.bindColor(@ColorRes color: Int) = ContextCompat.getColor(WoolworthsApplication.getAppContext(), color)

fun Fragment.bindColor(@ColorRes color: Int) = ContextCompat.getColor(WoolworthsApplication.getAppContext(), color)

fun Activity.bindString(@StringRes id: Int): String = WoolworthsApplication.getAppContext().resources.getString(id)

fun Fragment.bindString(@StringRes id: Int): String = WoolworthsApplication.getAppContext().resources.getString(id)

fun Fragment.getFuturaMediumFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets,"fonts/WFutura-Medium.ttf")

fun Fragment.getFuturaSemiBoldFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets,"fonts/WFutura-SemiBold.ttf")

