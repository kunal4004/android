package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.Transaction
import za.co.woolworths.financial.services.android.models.dto.account.TransactionHeader
import za.co.woolworths.financial.services.android.models.dto.account.TransactionItem
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.onboarding.OnBoardingFragment.Companion.ON_BOARDING_SCREEN_TYPE
import za.co.woolworths.financial.services.android.util.wenum.OnBoardingScreenType
import java.text.SimpleDateFormat

class KotlinUtils {
    companion object {
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

        fun updateCheckOutLink(jSessionId: String?){
            val checkoutLink = WoolworthsApplication.getCartCheckoutLink()
            val context = WoolworthsApplication.getAppContext()
            val packageManager = context.packageManager
            val packageInfo: PackageInfo = packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)

            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode else packageInfo.versionCode
            val appVersion = "$versionName.$versionCode"

            val checkOutLink = when(checkoutLink.contains("?")){
                true -> "$checkoutLink&appVersion=$appVersion&JSESSIONID=$jSessionId"
                else -> "$checkoutLink?appVersion=$appVersion&JSESSIONID=$jSessionId"
            }

            WoolworthsApplication.setCartCheckoutLink(checkOutLink)
        }

        fun postOneAppEvent(appScreen: String, featureName: String) {
            request(OneAppService.queryServicePostEvent(featureName, appScreen))
        }
    }
}