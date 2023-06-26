package za.co.woolworths.financial.services.android.ui.extension

import android.graphics.Typeface
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication


fun Any.bindDrawable(@DrawableRes drawableResource: Int) = ContextCompat.getDrawable(WoolworthsApplication.getAppContext(), drawableResource)

fun Any.bindColor(@ColorRes color: Int) = ContextCompat.getColor(WoolworthsApplication.getAppContext(), color)

fun Any.bindDimens(@DimenRes dimensIdRes: Int) = WoolworthsApplication.getAppContext()?.resources?.getDimension(dimensIdRes)

fun Any.bindString(@StringRes id: Int) = WoolworthsApplication.getAppContext().resources.getString(id)

fun Any.bindString(@StringRes id: Int, value: String = "", value1: String? = "", value2: String = "", value3: String = "", value4: String = "", value5: String = "", value6: String = "", value7: String = ""): String = WoolworthsApplication.getAppContext().resources.getString(id, value, value1, value2, value3, value4, value5, value6, value7)

fun Any.bindStringArray(@ArrayRes id: Int): Array<String>? = WoolworthsApplication.getAppContext()?.resources?.getStringArray(id)

fun Any.getFuturaMediumFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/WFutura-Medium.ttf")

fun Any.getFuturaSemiBoldFont(): Typeface = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/WFutura-SemiBold.ttf")

fun Any.getOpenSansSemiBoldFont(): Typeface? = Typeface.createFromAsset(WoolworthsApplication.getAppContext().assets, "fonts/OpenSans-SemiBold.ttf")

fun Any.deviceHeight() = WoolworthsApplication.getAppContext()?.resources?.displayMetrics?.heightPixels ?: 0

fun Any.deviceWidth() = WoolworthsApplication.getAppContext()?.resources?.displayMetrics?.widthPixels ?: 0
