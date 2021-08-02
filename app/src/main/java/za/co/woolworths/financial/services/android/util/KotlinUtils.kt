package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
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
import android.util.Pair
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.awfs.coordination.R
import com.google.common.reflect.TypeToken
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import org.json.JSONObject
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.WInternalWebPageActivity
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.*
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment.Companion.ON_BOARDING_SCREEN_TYPE
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.GeneralInfoDialogFragment
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType
import java.io.*
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class KotlinUtils {
    companion object {

        const val DELAY: Long = 900
        const val productImageUrlPrefix = "https://images.woolworthsstatic.co.za/"

        fun highlightTextInDesc(
                context: Context?,
                spannableTitle: SpannableString,
                searchTerm: String,
                textIsClickable: Boolean = true
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

        fun roundCornerDrawable(view: View, color: String?) {
            if (TextUtils.isEmpty(color)) return
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

        fun capitaliseFirstLetter(str: String): CharSequence? {
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
                screenType: OnBoardingScreenType
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

        fun toShipByDateFormat(date: Date?): String {
            return SimpleDateFormat("dd-MM-yyy").format(date)
        }

        fun presentEditDeliveryLocationActivity(
                activity: Activity?,
                requestCode: Int,
                deliveryType: DeliveryType? = null
        ) {
            var type = deliveryType
            if (type == null) {
                if (Utils.getPreferredDeliveryLocation() != null) {
                    type =
                            if (Utils.getPreferredDeliveryLocation().storePickup) DeliveryType.STORE_PICKUP else DeliveryType.DELIVERY
                }
            }
            activity?.apply {
                val mIntent = Intent(this, EditDeliveryLocationActivity::class.java)
                val mBundle = Bundle()
                mBundle.putString(EditDeliveryLocationActivity.DELIVERY_TYPE, type?.name)
                mIntent.putExtra("bundle", mBundle)
                startActivityForResult(mIntent, requestCode)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }

        fun setDeliveryAddressView(
                context: Activity?,
                shoppingDeliveryLocation: ShoppingDeliveryLocation,
                tvDeliveringTo: WTextView,
                tvDeliveryLocation: WTextView,
                deliverLocationIcon: ImageView?
        ) {
            with(shoppingDeliveryLocation) {
                when (storePickup) {
                    true -> {
                        tvDeliveringTo.text =
                                context?.resources?.getString(R.string.collecting_from)
                        tvDeliveryLocation.text =
                                context?.resources?.getString(R.string.store) + store?.name
                        tvDeliveryLocation.visibility = View.VISIBLE
                        deliverLocationIcon?.setBackgroundResource(R.drawable.icon_basket)
                    }
                    false -> {
                        tvDeliveringTo.text = context?.resources?.getString(R.string.delivering_to)
                        tvDeliveryLocation.text =
                                suburb.name + if (province?.name.isNullOrEmpty()) "" else ", " + province.name
                        tvDeliveryLocation.visibility = View.VISIBLE
                        deliverLocationIcon?.setBackgroundResource(R.drawable.icon_delivery)
                    }
                }
            }
        }

        fun updateCheckOutLink(jSessionId: String?) {
            val appVersionParam = "appVersion"
            val jSessionIdParam = "JSESSIONID"
            val checkoutLink = WoolworthsApplication.getCartCheckoutLink()

            val context = WoolworthsApplication.getAppContext()
            val packageManager = context.packageManager

            val packageInfo: PackageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)

            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toInt() else packageInfo.versionCode
            val appVersion = "$versionName.$versionCode"

            val symbolType= if(checkoutLink.contains("?")) "&" else "?"
            val checkOutLink = "$checkoutLink$symbolType$appVersionParam=$appVersion&$jSessionIdParam=$jSessionId"

            WoolworthsApplication.setCartCheckoutLinkWithParams(checkOutLink)
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
                emailMessage: String
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
                currencySymbol: String
        ): String =
                value.replace(groupingSeparator, "").replace(currencySymbol, "")

        fun parseMoneyValueWithLocale(
                locale: Locale,
                value: String,
                groupingSeparator: String,
                currencySymbol: String
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

        fun isOperatingHoursForInAppChat(tradingHours: MutableList<TradingHours>): Boolean? {
            val (_, opens, closes) = getInAppTradingHoursForToday(tradingHours)

            val now = Calendar.getInstance()
            val hour = now[Calendar.HOUR_OF_DAY] // Get hour in 24 hour format
            val minute = now[Calendar.MINUTE]

            val currentTime = WFormatter.parseDate("$hour:$minute")
            val openingTime = WFormatter.parseDate(opens)
            val closingTime = WFormatter.parseDate(closes)
            return currentTime.after(openingTime) && currentTime.before(closingTime)
        }

        fun getInAppTradingHoursForToday(tradingHours: MutableList<TradingHours>?): TradingHours {
            var tradingHoursForToday: TradingHours? = null
            tradingHours?.let {
                it.forEach { tradingHours ->
                    if (tradingHours.day.equals(Utils.getCurrentDay(), true)) {
                        tradingHoursForToday = tradingHours
                        return tradingHours
                    }
                }
            }
            return tradingHoursForToday ?: TradingHours("sunday", "00:00", "00:00")
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
                infoIcon: Int = 0
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
            urlString?.apply {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(this))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activity?.startActivity(intent)
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
            return Utils.getPreferredDeliveryLocation()?.storePickup == true
        }

        @SuppressLint("MissingPermission")
        @JvmStatic
        fun setUserPropertiesToNull() {
            val firebaseInstance =
                    FirebaseAnalytics.getInstance(WoolworthsApplication.getAppContext())
            firebaseInstance?.apply {
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_OFFERING,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_OFFERING,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.SILVER_CREDIT_CARD_PRODUCT_OFFERING,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.GOLD_CREDIT_CARD_PRODUCT_OFFERING,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.BLACK_CREDIT_CARD_PRODUCT_OFFERING,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.PERSONAL_LOAN_PRODUCT_STATE,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.CREDIT_CARD_PRODUCT_STATE,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.STORE_CARD_PRODUCT_STATE,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ATGId,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserProperty(
                        FirebaseManagerAnalyticsProperties.PropertyNames.C2ID,
                        FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable
                )
                setUserId(FirebaseManagerAnalyticsProperties.PropertyValues.notApplicable)
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

        fun openLinkInInternalWebView(activity: Activity?, url: String?) {
            activity?.apply {
                val openInternalWebView = Intent(this, WInternalWebPageActivity::class.java)
                openInternalWebView.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                openInternalWebView.putExtra("externalLink", url)
                startActivity(openInternalWebView)
            }
        }
    }

}