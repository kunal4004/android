package za.co.woolworths.financial.services.android.ui.extension

import android.graphics.Typeface
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication


fun Any.bindDrawable(@DrawableRes drawableResource: Int) = ContextCompat.getDrawable(WoolworthsApplication.getAppContext(), drawableResource)

fun Any.bindColor(@ColorRes color: Int) = ContextCompat.getColor(WoolworthsApplication.getAppContext(), color)

fun Any.bindString(@StringRes id: Int): String = WoolworthsApplication.getAppContext().resources.getString(id)

fun Any.bindStringArray(@ArrayRes id: Int): Array<String>? = WoolworthsApplication.getAppContext()?.resources?.getStringArray(id)

fun Any.getFuturaMediumFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/WFutura-Medium.ttf")

fun Any.getFuturaSemiBoldFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/WFutura-SemiBold.ttf")

fun Any.getMyriadProSemiBoldFont(): Typeface? = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/MyriadPro-Semibold.otf")

fun Any.deviceHeight() = WoolworthsApplication.getAppContext()?.resources?.displayMetrics?.heightPixels ?: 0

fun Any.deviceWidth() = WoolworthsApplication.getAppContext()?.resources?.displayMetrics?.widthPixels ?: 0
