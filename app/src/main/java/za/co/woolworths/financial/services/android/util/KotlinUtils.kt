package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
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
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import com.awfs.coordination.R
import org.json.JSONObject
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment.Companion.ON_BOARDING_SCREEN_TYPE
import za.co.woolworths.financial.services.android.ui.views.WTextView
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class KotlinUtils {
    companion object {

        const val productImageUrlPrefix = "https://images.woolworthsstatic.co.za/"

        fun highlightTextInDesc(context: Context?, spannableTitle: SpannableString, searchTerm: String, textIsClickable: Boolean = true): SpannableString {
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
            if (textIsClickable) spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val dimenPix =
                    context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
            typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                    ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                    ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannableTitle
        }

        fun setTransparentStatusBar(appCompatActivity: AppCompatActivity?) {

            if (Build.VERSION.SDK_INT >= 19) {
                appCompatActivity?.window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                appCompatActivity?.setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
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
            val activity = WoolworthsApplication.getInstance()?.currentActivity
            val resId: Int =
                    activity?.resources?.getIdentifier("status_bar_height", "dimen", "android")
                            ?: -1
            var statusBarHeight = 0
            if (resId > 0) {
                statusBarHeight = activity?.resources?.getDimensionPixelSize(resId) ?: 0
            }
            return statusBarHeight + actionBarHeight
        }

        fun getStatusBarHeight(appCompatActivity: AppCompatActivity?): Int {
            var result = 0
            val resourceId =
                    appCompatActivity?.resources?.getIdentifier("status_bar_height", "dimen", "android")
                            ?: 0
            if (resourceId > 0) {
                result = appCompatActivity?.resources?.getDimensionPixelSize(resourceId) ?: 0
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
                    floatArrayOf(paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp, paddingDp)
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
            val words = str.split(" ").toMutableList()
            var output = ""
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

        fun getToolbarHeight(appCompatActivity: AppCompatActivity?): Int {
            val tv = TypedValue()
            var actionBarHeight = 0
            if (appCompatActivity?.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true)!!) {
                actionBarHeight =
                        TypedValue.complexToDimensionPixelSize(tv.data, appCompatActivity.resources?.displayMetrics)
            }
            return actionBarHeight
        }

        fun addSpaceBeforeUppercase(word: String?): String {
            var newWord = ""
            word?.forEach { alphabet -> newWord += if (alphabet.isUpperCase()) " $alphabet" else alphabet }
            return newWord
        }

        fun setAccountNavigationGraph(navigationController: NavController, screenType: OnBoardingScreenType) {
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
                transactionMap.value.forEach { transactionItem -> transactionList.add(transactionItem) }
            }

            return transactionList
        }

        fun presentEditDeliveryLocationActivity(activity: Activity?, requestCode: Int, deliveryType: DeliveryType? = null) {
            var type = deliveryType
            if (type == null) {
                if (Utils.getPreferredDeliveryLocation() != null) {
                    type = if (Utils.getPreferredDeliveryLocation().suburb.storePickup) DeliveryType.STORE_PICKUP else DeliveryType.DELIVERY
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

        fun setDeliveryAddressView(context: Activity?, shoppingDeliveryLocation: ShoppingDeliveryLocation, tvDeliveringTo: WTextView, tvDeliveryLocation: WTextView, deliverLocationIcon: ImageView?) {
            with(shoppingDeliveryLocation) {
                when (suburb.storePickup) {
                    true -> {
                        tvDeliveringTo.text = context?.resources?.getString(R.string.collecting_from)
                        tvDeliveryLocation.text = context?.resources?.getString(R.string.store) + suburb.name
                        tvDeliveryLocation.visibility = View.VISIBLE
                        deliverLocationIcon?.setBackgroundResource(R.drawable.icon_basket)
                    }
                    false -> {
                        tvDeliveringTo.text = context?.resources?.getString(R.string.delivering_to)
                        tvDeliveryLocation.text = suburb.name + if (province?.name.isNullOrEmpty()) "" else ", " + province.name
                        tvDeliveryLocation.visibility = View.VISIBLE
                        deliverLocationIcon?.setBackgroundResource(R.drawable.icon_delivery)
                    }
                }
            }
        }

        fun updateCheckOutLink(jSessionId: String?) {
            val checkoutLink = WoolworthsApplication.getCartCheckoutLink()
            val context = WoolworthsApplication.getAppContext()
            val packageManager = context.packageManager
            val packageInfo: PackageInfo =
                    packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)

            val versionName = packageInfo.versionName
            val versionCode =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toInt() else packageInfo.versionCode
            val appVersion = "$versionName.$versionCode"

            val checkOutLink = when (checkoutLink.contains("?")) {
                true -> "$checkoutLink&appVersion=$appVersion&JSESSIONID=$jSessionId"
                else -> "$checkoutLink?appVersion=$appVersion&JSESSIONID=$jSessionId"
            }

            WoolworthsApplication.setCartCheckoutLink(checkOutLink)
        }

        fun sendEmail(activity: Activity?, emailId: String, subject: String?) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:" + emailId +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(""))
            val listOfEmail =
                    activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.INFO, bindString(R.string.contact_us_no_email_error).replace("email_address", emailId).replace("subject_line", subject
                        ?: ""))
            }
        }

        fun sendEmail(activity: Activity?, emailAddress: String, subjectLine: String?, emailMessage: String) {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:" + emailAddress +
                    "?subject=" + Uri.encode(subjectLine) +
                    "&body=" + Uri.encode(emailMessage))
            val listOfEmail =
                    activity?.packageManager?.queryIntentActivities(emailIntent, 0) ?: arrayListOf()
            if (listOfEmail.size > 0) {
                activity?.startActivity(emailIntent)
            } else {
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.INFO, activity?.resources?.getString(R.string.contact_us_no_email_error)?.replace("email_address", emailAddress)?.replace("subject_line", subjectLine
                        ?: ""))
            }
        }

        fun postOneAppEvent(appScreen: String, featureName: String) {
            request(OneAppService.queryServicePostEvent(featureName, appScreen))
        }

        fun isItemsQuantityForClickAndCollectExceed(totalItemsCount: Int): Boolean {
            WoolworthsApplication.getClickAndCollect()?.maxNumberOfItemsAllowed?.let { maxAllowedQuantity ->
                Utils.getPreferredDeliveryLocation()?.suburb?.let { suburb ->
                    return (totalItemsCount > maxAllowedQuantity && suburb.storePickup)
                }
            }
            return false
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
            val capitaliseFirstLetterInName = name?.substring(0, 1)?.toUpperCase(Locale.getDefault())
            val lowercaseOtherLetterInName = name?.substring(1, name.length)?.toLowerCase(Locale.getDefault())
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

        fun getInAppTradingHoursForToday(tradingHours: MutableList<TradingHours>): TradingHours {
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
            if (view?.isClickable != true) return
            view.isClickable = false
            view.postDelayed({ view.isClickable = true }, AppConstant.DELAY_900_MS)
        }
    }
}