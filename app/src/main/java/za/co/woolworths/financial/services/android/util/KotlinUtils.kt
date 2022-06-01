package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.*
import android.text.style.*
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.common.reflect.TypeToken
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import org.json.JSONObject
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.KEY_COLLECTING_DETAILS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dao.SessionDao.KEY
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.models.dto.app_config.chat.ConfigTradingHours
import za.co.woolworths.financial.services.android.models.dto.cart.FulfillmentDetails
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.WInternalWebPageActivity
import za.co.woolworths.financial.services.android.ui.activities.account.LinkDeviceConfirmationActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.*
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountsFragment
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AbsaApiFailureHandler
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment.Companion.ON_BOARDING_SCREEN_TYPE
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GeneralInfoDialogFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_DASH_TAB
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType
import java.io.*
import java.net.SocketException
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class KotlinUtils {
    companion object {

        var placeId: String? = null
        var isLocationSame: Boolean? = false
        var isDeliveryLocationTabClicked: Boolean? = false
        var isCncTabClicked: Boolean? = false
        var isDashTabClicked: Boolean? = false
        var isComingFromCncTab: Boolean? = false
        var browsingDeliveryType: Delivery? = getPreferredDeliveryType()
        @JvmStatic
        var browsingCncStore: Store? = null
        const val DELAY: Long = 900
        const val collectionsIdUrl = "woolworths.wfs.co.za/CustomerCollections/IdVerification"
        const val COLLECTIONS_EXIT_URL = "collectionsExitUrl"
        const val TREATMENT_PLAN = "treamentPlan"
        const val RESULT_CODE_CLOSE_VIEW = 2203
        var GEO_REQUEST_CODE = -1


        fun highlightTextInDesc(
            context: Context?,
            spannableTitle: SpannableString,
            searchTerm: String,
            textIsClickable: Boolean = true,
        ): SpannableString {
            var start = spannableTitle.indexOf(searchTerm)
            if (start == -1) {
                start = 0
            }

            val end = start + searchTerm.length
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Utils.makeCall(searchTerm)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            val typeface: Typeface? =
                context?.let { ResourcesCompat.getFont(it, R.font.myriad_pro_semi_bold_otf) }
            if (textIsClickable) spannableTitle.setSpan(
                clickableSpan,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val dimenPix =
                context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
            typeface?.style?.let { style ->
                spannableTitle.setSpan(
                    StyleSpan(style),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            spannableTitle.setSpan(
                AbsoluteSizeSpan(
                    dimenPix?.toInt()
                        ?: 0
                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableTitle.setSpan(
                AbsoluteSizeSpan(
                    dimenPix?.toInt()
                        ?: 0
                ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spannableTitle
        }

        fun setTransparentStatusBar(appCompatActivity: AppCompatActivity?) {

            if (Build.VERSION.SDK_INT >= 19) {
                appCompatActivity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                appCompatActivity?.setWindowFlag(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    false
                )
                appCompatActivity?.window?.statusBarColor = Color.TRANSPARENT
            }
        }

        private fun AppCompatActivity.setWindowFlag(bits: Int, on: Boolean) {
            val winParams = window?.attributes
            winParams?.apply {
                flags = if (on) {
                    flags or bits
                } else {
                    flags and bits.inv()
                }
                window?.attributes = winParams
            }
        }

        fun getStatusBarHeight(actionBarHeight: Int): Int {
            val resources = WoolworthsApplication.getAppContext().resources
            val resId: Int = resources?.getIdentifier("status_bar_height", "dimen", "android")
                ?: -1
            var statusBarHeight = 0
            if (resId > 0) {
                statusBarHeight = resources?.getDimensionPixelSize(resId) ?: 0
            }
            return statusBarHeight + actionBarHeight
        }

        fun getStatusBarHeight(): Int {
            var result = 0
            val resources = WoolworthsApplication.getAppContext().resources
            val resourceId = resources?.getIdentifier("status_bar_height", "dimen", "android") ?: 0
            if (resourceId > 0) {
                result = resources?.getDimensionPixelSize(resourceId) ?: 0
            }
            return result
        }

        fun onBackPressed(activity: Activity?) {
            activity?.apply {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
        }

        fun interpolateColor(fraction: Float, startValue: Int, endValue: Int): Int {
            val startA = startValue shr 24 and 0xff
            val startR = startValue shr 16 and 0xff
            val startG = startValue shr 8 and 0xff
            val startB = startValue and 0xff
            val endA = endValue shr 24 and 0xff
            val endR = endValue shr 16 and 0xff
            val endG = endValue shr 8 and 0xff
            val endB = endValue and 0xff
            return startA + (fraction * (endA - startA)).toInt() shl 24 or
                    (startR + (fraction * (endR - startR)).toInt() shl 16) or
                    (startG + (fraction * (endG - startG)).toInt() shl 8) or
                    startB + (fraction * (endB - startB)).toInt()
        }

        fun roundCornerDrawable(view: View?, color: String?) {
            if (view == null || TextUtils.isEmpty(color)) return
            val paddingDp: Float = (12 * view.context.resources.displayMetrics.density)
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadii =
                floatArrayOf(
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp,
                    paddingDp
                )
            shape.setColor(Color.parseColor(color))
            view.background = shape
        }

        fun dpToPxConverter(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }

        fun pxToDpConverter(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

        fun convertFromDateToDate(date: String?): String? {
            date?.apply {
                val fromDateFormat = SimpleDateFormat("yyyy-MM-dd")
                val toDateFormat = SimpleDateFormat("MMMM yyyy")
                val fromDate = fromDateFormat.parse(date)
                return toDateFormat.format(fromDate)
            }
            return ""
        }

        fun capitaliseFirstLetter(str: String?): CharSequence? {
            if (str.isNullOrEmpty())
                return str
            val value = str.toLowerCase()
            val words = value.split(" ").toMutableList()
            var output = ""
            for (word in words) {
                output += word.capitalize() + " "
            }
            return output.trim()
        }


        fun capitaliseFirstWordAndLetters(str: String): CharSequence? {
            val value = str.toLowerCase()
            val words = value.split(" ").toMutableList()

            var output = words[0].toUpperCase() + " "
            words.removeAt(0)
            for (word in words) {
                output += word.capitalize() + " "
            }
            return output.trim()
        }

        fun isNumberPositive(i: Float): Boolean {
            return when {
                i < 0 -> true
                i > 0 -> false
                else -> false
            }
        }

        fun getToolbarHeight(): Int {
            val tv = TypedValue()
            var actionBarHeight = 0
            val appCompat = WoolworthsApplication.getAppContext()
            if (appCompat?.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true)!!) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(
                    tv.data,
                    appCompat.resources?.displayMetrics
                )
            }
            return actionBarHeight
        }

        fun addSpaceBeforeUppercase(word: String?): String {
            var newWord = ""
            word?.forEach { alphabet -> newWord += if (alphabet.isUpperCase()) " $alphabet" else alphabet }
            return newWord
        }

        fun setAccountNavigationGraph(
            navigationController: NavController,
            screenType: OnBoardingScreenType,
        ) {
            val bundle = Bundle()
            bundle.putSerializable(ON_BOARDING_SCREEN_TYPE, screenType)
            navigationController.setGraph(navigationController.graph, bundle)
        }

        /***
         * Convert response to a list of transactions with TransactionHeader()
         * and TransactionItem() that inherits Transaction
         */
        @SuppressLint("SimpleDateFormat")
        fun getListOfTransaction(transactionItemList: MutableList<TransactionItem>?): MutableList<Transaction> {

            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat = SimpleDateFormat("MMMM yyyy")

            //Setting Date to the format dd/MM/yyyy
            val timeFormat = SimpleDateFormat("dd / MM / yyyy")

            transactionItemList?.forEach { transactionItem ->
                val transactionDate = transactionItem.date
                val inputDate = transactionDate?.let { date -> inputFormat.parse(date) }
                val outputMonthYear = inputDate?.let { date -> outputFormat.format(date) }
                transactionItem.month = outputMonthYear

                val formattedDate = timeFormat.format(inputDate)
                transactionItem.date = formattedDate
            }

            val groupTransactionsByMonth = transactionItemList?.groupBy { it.month }

            val transactionList: MutableList<Transaction> = mutableListOf()

            groupTransactionsByMonth?.forEach { transactionMap ->
                transactionList.add(TransactionHeader(transactionMap.key))
                transactionMap.value.forEach { transactionItem ->
                    transactionList.add(
                        transactionItem
                    )
                }
            }

            return transactionList
        }

        fun getDateDaysAfter(daysAfter: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, daysAfter)
            return calendar.time
        }

        fun removeRandFromAmount(amount: String): String {
            if (amount.contains("R")) {
                return amount.substring(1)
            }
            return amount
        }

        fun toShipByDateFormat(date: Date?): String {
            return SimpleDateFormat("dd-MM-yyy").format(date)
        }

        fun presentEditDeliveryGeoLocationActivity(
            activity: Activity?,
            requestCode: Int,
            delivery: Delivery? = Delivery.STANDARD,
            placeId: String? = null,
            isFromDashTab: Boolean = false,
            isComingFromCheckout: Boolean = false,
            isComingFromSlotSelection: Boolean = false,
            savedAddressResposne: SavedAddressResponse? = null,
            defaultAddress: Address? = null,
            whoISCollecting: String? = null,
        ) {

            activity?.apply {
                val mIntent = Intent(this, EditDeliveryLocationActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString(DELIVERY_TYPE, delivery.toString())
                mBundle.putString(PLACE_ID, placeId)
                mBundle.putBoolean(IS_FROM_DASH_TAB, isFromDashTab)
                mBundle.putBoolean(IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
                mBundle.putBoolean(IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
                mBundle.putSerializable(SAVED_ADDRESS_RESPONSE, savedAddressResposne)
                mBundle.putSerializable(DEFAULT_ADDRESS, defaultAddress)
                mBundle.putString(KEY_COLLECTING_DETAILS, whoISCollecting)
                mIntent.putExtra(BUNDLE, mBundle)
                GEO_REQUEST_CODE = requestCode
                startActivityForResult(mIntent, requestCode)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }

        fun setDeliveryAddressView(
            context: Activity?,
            fulfillmentDetails: FulfillmentDetails,
            tvDeliveringTo: TextView,
            tvDeliveryLocation: TextView,
            deliverLocationIcon: ImageView?,
        ) {
            with(fulfillmentDetails) {
                when (Delivery?.getType(deliveryType)) {
                    Delivery.CNC -> {
                        tvDeliveringTo?.text =
                            context?.resources?.getString(R.string.collecting_from)
                        tvDeliveryLocation?.text =
                            capitaliseFirstLetter(context?.resources?.getString(R.string.store) + storeName)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_collection_circle)
                    }
                    Delivery.STANDARD -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        tvDeliveryLocation?.text = capitaliseFirstLetter(address?.address1 ?: "")

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }
                    Delivery.DASH -> {
                        val timeSlot: String? =
                            WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime
                        if (timeSlot == null) {
                            tvDeliveringTo?.text =
                                context?.resources?.getString(R.string.dash_delivery_bold)
                        } else {
                            tvDeliveringTo?.text =
                                context?.resources?.getString(R.string.dash_delivery_bold)
                                    .plus("\t" + timeSlot)
                        }
                        tvDeliveryLocation?.text =
                            capitaliseFirstLetter(WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                ?: address?.address1 ?: "")
                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_dash_delivery_circle)
                    }
                    else -> {
                        tvDeliveringTo.text =
                            context?.resources?.getString(R.string.standard_delivery)
                        tvDeliveryLocation?.text =
                            context?.resources?.getString(R.string.default_location)

                        tvDeliveryLocation?.visibility = View.VISIBLE
                        deliverLocationIcon?.setImageResource(R.drawable.ic_delivery_circle)
                    }
                }
            }
        }

        fun updateCheckOutLink(jSessionId: String?) {
            val appVersionParam = "appVersion"
            val jSessionIdParam = "JSESSIONID"
            val checkoutLink = AppConfigSingleton.cartCheckoutLink
            val context = WoolworthsApplication.getAppContext()
            val packageManager = context.packageManager

            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)

            val versionName = packageInfo.versionName
            val versionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toInt() else packageInfo.versionCode
            val appVersion = "$versionName.$versionCode"

            if (checkoutLink != null) {
                val symbolType = if (checkoutLink.contains("?")) "&" else "?"
                WoolworthsApplication.setCartCheckoutLinkWithParams("$checkoutLink$symbolType$appVersionParam=$appVersion&$jSessionIdParam=$jSessionId")
            }
        }

        fun sendEmail(activity: Activity?, emailId: String, subject: String?) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(
                "mailto:" + emailId +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode("")
            )
            val listOfEmail =
                activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    bindString(R.string.contact_us_no_email_error).replace("email_address", emailId)
                        .replace(
                            "subject_line", subject
                                ?: ""
                        )
                )
            }
        }

        fun sendEmail(
            activity: Activity?,
            emailAddress: String,
            subjectLine: String?,
            emailMessage: String,
        ) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(
                "mailto:" + emailAddress +
                        "?subject=" + Uri.encode(subjectLine) +
                        "&body=" + Uri.encode(emailMessage)
            )
            val listOfEmail =
                activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(
                    activity,
                    CustomPopUpWindow.MODAL_LAYOUT.INFO,
                    activity?.resources?.getString(R.string.contact_us_no_email_error)
                        ?.replace("email_address", emailAddress)?.replace(
                            "subject_line", subjectLine
                                ?: ""
                        )
                )
            }
        }

        fun postOneAppEvent(appScreen: String, featureName: String) {
            request(OneAppService.queryServicePostEvent(featureName, appScreen))
        }

        fun parseMoneyValue(
            value: String,
            groupingSeparator: String,
            currencySymbol: String,
        ): String =
            value.replace(groupingSeparator, "").replace(currencySymbol, "")

        fun parseMoneyValueWithLocale(
            locale: Locale,
            value: String,
            groupingSeparator: String,
            currencySymbol: String,
        ): Number {
            val valueWithoutSeparator = parseMoneyValue(value, groupingSeparator, currencySymbol)
            return try {
                NumberFormat.getInstance(locale).parse(valueWithoutSeparator)!!
            } catch (exception: ParseException) {
                0
            }
        }

        fun getLocaleFromTag(localeTag: String): Locale {
            return try {
                Locale.Builder().setLanguageTag(localeTag).build()
            } catch (e: IllformedLocaleException) {
                Locale.getDefault()
            }
        }

        fun highlightText(string: String, keys: MutableList<String>?): SpannableStringBuilder {
            val noteStringBuilder = SpannableStringBuilder(string)
            keys?.forEach { key ->
                val start = string.indexOf(key)
                val end = start.plus(key.length)
                val myriadProFont: TypefaceSpan = CustomTypefaceSpan("", getMyriadProSemiBoldFont())
                noteStringBuilder.setSpan(
                    myriadProFont,
                    start,
                    end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
                noteStringBuilder.setSpan(
                    ForegroundColorSpan(bindColor(R.color.description_color)),
                    start,
                    end,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

            return noteStringBuilder
        }

        fun getJSONFileFromRAWResFolder(context: Context?, @RawRes id: Int): JSONObject {
            val awsConfiguration: InputStream? = context?.resources?.openRawResource(id)
            val writer: Writer = StringWriter()
            val buffer = CharArray(1024)
            awsConfiguration?.use { config ->
                val reader: Reader = BufferedReader(InputStreamReader(config, "UTF-8"))
                var n: Int
                while (reader.read(buffer).also {
                        n = it
                    } != -1) {
                    writer.run { write(buffer, 0, n) }
                }
            }
            val awsConfigurationEnvelop = writer.toString()
            return JSONObject(awsConfigurationEnvelop)
        }

        fun firstLetterCapitalization(name: String?): String? {
            val capitaliseFirstLetterInName =
                name?.substring(0, 1)?.toUpperCase(Locale.getDefault())
            val lowercaseOtherLetterInName =
                name?.substring(1, name.length)?.toLowerCase(Locale.getDefault())
            return capitaliseFirstLetterInName?.plus(lowercaseOtherLetterInName)
        }

        fun isOperatingHoursForInAppChat(tradingHours: MutableList<ConfigTradingHours>): Boolean? {
            val (_, opens, closes) = getInAppTradingHoursForToday(tradingHours)

            val now = Calendar.getInstance()
            val hour = now[Calendar.HOUR_OF_DAY] // Get hour in 24 hour format
            val minute = now[Calendar.MINUTE]

            val currentTime = WFormatter.parseDate("$hour:$minute")
            val openingTime = WFormatter.parseDate(opens)
            val closingTime = WFormatter.parseDate(closes)
            return currentTime.after(openingTime) && currentTime.before(closingTime)
        }

        fun getInAppTradingHoursForToday(tradingHours: MutableList<ConfigTradingHours>?): ConfigTradingHours {
            var tradingHoursForToday: ConfigTradingHours? = null
            tradingHours?.let {
                it.forEach { tradingHours ->
                    if (tradingHours.day.equals(Utils.getCurrentDay(), true)) {
                        tradingHoursForToday = tradingHours
                        return tradingHours
                    }
                }
            }
            return tradingHoursForToday ?: ConfigTradingHours("sunday", "00:00", "00:00")
        }

        fun avoidDoubleClicks(view: View?) {
            view?.apply {
                if (!isClickable) return
                isClickable = false
                GlobalScope.doAfterDelay(AppConstant.DELAY_900_MS) {
                    isClickable = true
                }
            }
        }

        /**
         * Calling the convertToTranslucent method on platforms after Android 5.0
         */
        @SuppressLint("DiscouragedPrivateApi")
        fun convertActivityToTranslucent(activity: Activity) {
            try {
                val getActivityOptions =
                    Activity::class.java.getDeclaredMethod("getActivityOptions")
                getActivityOptions.isAccessible = true
                val options = getActivityOptions.invoke(activity)
                val classes = Activity::class.java.declaredClasses
                var translucentConversionListenerClazz: Class<*>? = null
                for (clazz in classes) {
                    if (clazz.simpleName.contains("TranslucentConversionListener")) {
                        translucentConversionListenerClazz = clazz
                    }
                }
                val convertToTranslucent = Activity::class.java.getDeclaredMethod(
                    "convertToTranslucent",
                    translucentConversionListenerClazz, ActivityOptions::class.java
                )
                convertToTranslucent.isAccessible = true
                convertToTranslucent.invoke(activity, null, options)
            } catch (t: Throwable) {
                FirebaseManager.logException(t)
            }
        }

        fun showGeneralInfoDialog(
            fragmentManager: FragmentManager,
            description: String,
            title: String = "",
            actionText: String = "",
            infoIcon: Int = 0,
        ) {
            val dialog =
                GeneralInfoDialogFragment.newInstance(description, title, actionText, infoIcon)
            fragmentManager.let { fragmentTransaction ->
                dialog.show(
                    fragmentTransaction,
                    GeneralInfoDialogFragment::class.java.simpleName
                )
            }
        }

        fun openUrlInPhoneBrowser(urlString: String?, activity: Activity?) {
            try {
                urlString?.apply {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    activity?.startActivity(intent)
                }
            } catch (exception: ActivityNotFoundException) {
                FirebaseManager.logException("no browser found - $exception")
                activity?.apply {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder
                        .setTitle(R.string.browser_not_found_title)
                        .setMessage(R.string.browser_not_found_msg)
                        .setCancelable(true)
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }

        fun openApplicationSettings(requestCode: Int, activity: Activity?) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            activity?.startActivityForResult(intent, requestCode)
        }

        fun openAccessMyLocationDeviceSettings(requestCode: Int, activity: Activity?) {
            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity?.startActivityForResult(locIntent, requestCode)
            activity?.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }

        fun getAccount(accountExtras: String?): Pair<ApplyNowState, Account>? {
            return Gson().fromJson<Pair<ApplyNowState, Account>>(
                accountExtras,
                object : TypeToken<Pair<ApplyNowState?, Account?>?>() {}.type
            )
        }

        fun isAppInstalled(activity: Activity?, appURI: String?): Boolean {
            activity?.apply {
                return appURI?.let { this.packageManager.getLaunchIntentForPackage(it) } != null
            }
            return false
        }

        fun isDeliveryOptionClickAndCollect(): Boolean {
            return getPreferredDeliveryType() == Delivery.CNC
        }

        @SuppressLint("MissingPermission")
        @JvmStatic
        fun setUserPropertiesToNull() {
            val firebaseInstance =
                FirebaseAnalytics.getInstance(WoolworthsApplication.getAppContext())
            firebaseInstance?.apply {
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.CREDIT_CARD_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_STATE,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.ATGId,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserProperty(
                    FirebaseManagerAnalyticsProperties.PropertyNames.C2ID,
                    FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE
                )
                setUserId(FirebaseManagerAnalyticsProperties.PropertyValues.NOT_APPLICABLE)
            }
        }

        fun getUserDefinedDeviceName(activity: Activity?): String {
            var deviceName = ""
            activity?.apply {
                try {
                    if (TextUtils.isEmpty(deviceName)) {
                        deviceName = Settings.Secure.getString(contentResolver, "bluetooth_name")
                    }
                } catch (e: Exception) {
                }
            }
            return deviceName
        }

        fun rotateViewAnimation(): RotateAnimation {
            val animation = RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            )
            with(animation) {
                duration = AppConstant.DURATION_1000_MS
                repeatCount = 45
                repeatCount = Animation.INFINITE
            }
            return animation
        }

        fun getTimeStamp(): String? {
            return try {
                java.lang.String.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(
                        System.currentTimeMillis()
                    )
                )
            } catch (ex: java.lang.Exception) {
                null
            }

        }

        /**
         * This function should satisfy below conditions
         * - It should not be a store pick up &&
         * - Selected suburb [AppInstanceObject.User.preferredShoppingDeliveryLocation] should match up with suburb id's in mobile config
         * @return Returns boolean value indicating if current suburb delivers liquors
         * [Boolean.true]  if above conditions match else [Boolean.false]
         *
         * @see [za.co.woolworths.financial.services.android.models.dao.AppInstanceObject.User.preferredShoppingDeliveryLocation]
         */
        fun isCurrentSuburbDeliversLiquor(): Boolean {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.liquorDeliverable == true
        }

        /**
         * This function should satisfy below conditions
         * - Retrieve [SessionDao.KEY.LIQUOR_MODAL_SHOWN] flag from SessionDao database
         *
         * @return Returns boolean value indicating if liquor selection modal is shown
         * [Boolean.true]  if above conditions match else [Boolean.false]
         *
         * @see SessionDao
         */
        fun isLiquorModalShown(): Boolean {
            val firstTime = Utils.getSessionDaoValue(KEY.LIQUOR_MODAL_SHOWN)
            return firstTime != null
        }

        fun setLiquorModalShown() {
            try {
                val firstTime = Utils.getSessionDaoValue(KEY.LIQUOR_MODAL_SHOWN)
                if (firstTime == null) {
                    Utils.sessionDaoSave(KEY.LIQUOR_MODAL_SHOWN, "1")
                }
            } catch (ignored: NullPointerException) {
            }
        }

        fun openLinkInInternalWebView(
            activity: Activity?,
            url: String?,
            treatmentPlan: Boolean,
            collectionsExitUrl: String?,
        ) {
            activity?.apply {
                val openInternalWebView = Intent(this, WInternalWebPageActivity::class.java)
                openInternalWebView.putExtra("externalLink", url)
                if (treatmentPlan) {
                    openInternalWebView.putExtra(TREATMENT_PLAN, treatmentPlan)
                    openInternalWebView.putExtra(COLLECTIONS_EXIT_URL, collectionsExitUrl)
                    startActivityForResult(openInternalWebView, RESULT_CODE_CLOSE_VIEW)
                } else {
                    openInternalWebView.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(openInternalWebView)
                }
            }
        }

        fun openTreatmentPlanUrl(activity: Activity?, eligibilityPlan: EligibilityPlan?) {
            var collectionUrlFromConfig: Pair<String?, String?>? = null
            var exitUrl: String? = ""
            val accountOptions = AppConfigSingleton.accountOptions

            when (eligibilityPlan?.productGroupCode) {
                ProductGroupCode.SC -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.storeCard?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.storeCard?.collectionsDynamicUrl
                    exitUrl = accountOptions?.showTreatmentPlanJourney?.storeCard?.exitUrl
                }

                ProductGroupCode.PL -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.personalLoan?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.personalLoan?.collectionsDynamicUrl
                    exitUrl = accountOptions?.showTreatmentPlanJourney?.personalLoan?.exitUrl
                }

                ProductGroupCode.CC -> {
                    collectionUrlFromConfig =
                        accountOptions?.collectionsStartNewPlanJourney?.creditCard?.collectionsUrl to accountOptions?.showTreatmentPlanJourney?.creditCard?.collectionsDynamicUrl
                    exitUrl = accountOptions?.collectionsStartNewPlanJourney?.creditCard?.exitUrl
                }
            }

            /**
             *  Use dynamic collection url when ("collectionsViewExistingPlan")
             *  else use collection url
             */
            val finalCollectionUrlFromConfig =
                when (eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value
                        || eligibilityPlan?.actionText == ActionText.VIEW_ELITE_PLAN.value) {
                    true -> collectionUrlFromConfig?.second
                    false -> collectionUrlFromConfig?.first
                }

            val url = finalCollectionUrlFromConfig + eligibilityPlan?.appGuid

            openLinkInInternalWebView(
                activity,
                url,
                true,
                exitUrl
            )
        }

        fun linkDeviceIfNecessary(
            activity: Activity?,
            state: ApplyNowState,
            doJob: () -> Unit,
            elseJob: () -> Unit,
        ) {
            if (MyAccountsFragment.verifyAppInstanceId() &&
                (Utils.isGooglePlayServicesAvailable() ||
                        Utils.isHuaweiMobileServicesAvailable())
            ) {
                doJob()
                activity?.let {
                    val intent = Intent(it, LinkDeviceConfirmationActivity::class.java)
                    intent.putExtra(AccountSignedInPresenterImpl.APPLY_NOW_STATE, state)
                    it.startActivity(intent)
                    it.overridePendingTransition(R.anim.slide_up_fast_anim, R.anim.stay)
                }
            } else {
                elseJob()
            }
        }

        fun getPreferredDeliveryType(): Delivery? {
            return Delivery.getType(
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType ?: ""
            )
        }

        fun getDeliveryType(): FulfillmentDetails? {
            return if (SessionUtilities.getInstance().isUserAuthenticated) {
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails
            } else{
                getAnonymousUserLocationDetails()?.fulfillmentDetails
            }
        }

        fun getPreferredPlaceId(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId ?: ""
        }

        private fun getPreferredStoreName(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.storeName ?: ""
        }

        fun getPreferredDeliveryAddress(): String {
            return Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.address1 ?: ""
        }

        fun getPreferredDeliveryAddressOrStoreName(): String {
            return when (getPreferredDeliveryType()) {
                Delivery.CNC, Delivery.STANDARD, Delivery.DASH -> getPreferredStoreName()
                else -> ""
            }
        }

        fun retrieveFulfillmentStoreId(fulFillmentTypeId: String): String {
            var fulFillmentStoreId: String = ""
            var typeId = fulFillmentTypeId
            if (typeId.length == 1)
                typeId = "0$typeId"
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.fulfillmentStores?.let {
                val details = Gson().fromJson<Map<String, String>>(
                    it,
                    object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                )
                fulFillmentStoreId = details?.get(typeId) ?: ""
            }
            return fulFillmentStoreId
        }

        fun getUniqueDeviceID(result: (String?) -> Unit) {
            val deviceID = Utils.getSessionDaoValue(KEY.DEVICE_ID)
            when (deviceID.isNullOrEmpty()) {
                true -> {
                    FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val resultId = task.result
                            Utils.sessionDaoSave(KEY.DEVICE_ID, resultId)
                            result(resultId)
                        }

                    }
                }
                false -> result(deviceID)
            }
        }


        fun lowercaseEditText(editText: EditText) {
            editText.filters = arrayOf<InputFilter>(
                object : InputFilter.AllCaps() {
                    override fun filter(
                        source: CharSequence,
                        start: Int,
                        end: Int,
                        dest: Spanned?,
                        dstart: Int,
                        dend: Int,
                    ): CharSequence {
                        return source.toString().lowercase()
                    }
                }
            )
        }

        fun saveAnonymousUserLocationDetails(shoppingDeliveryLocation: ShoppingDeliveryLocation) {
            Utils.sessionDaoSave(KEY.ANONYMOUS_USER_LOCATION_DETAILS,
                Utils.objectToJson(shoppingDeliveryLocation))
        }

        fun getAnonymousUserLocationDetails(): ShoppingDeliveryLocation? {
            var location: ShoppingDeliveryLocation? = null
            try {
                SessionDao.getByKey(KEY.ANONYMOUS_USER_LOCATION_DETAILS).value?.let {
                    location = Utils.strToJson(it,
                        ShoppingDeliveryLocation::class.java) as ShoppingDeliveryLocation?
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
            }
            return location
        }

        fun clearAnonymousUserLocationDetails() {
            Utils.removeFromDb(KEY.ANONYMOUS_USER_LOCATION_DETAILS)
        }

        fun coroutineContextWithExceptionHandler(errorHandler: (AbsaApiFailureHandler) -> Unit): CoroutineContext {
            return (Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
                when (throwable) {
                    is SocketException -> errorHandler(AbsaApiFailureHandler.NoInternetApiFailure)
                    is HttpException -> errorHandler(AbsaApiFailureHandler.HttpException(throwable.message(),
                        throwable.code()))
                    is Exception -> errorHandler(AbsaApiFailureHandler.Exception(throwable.message,
                        throwable.hashCode()))
                    else -> errorHandler(AbsaApiFailureHandler.NoInternetApiFailure)
                }
            })
        }
    }
}