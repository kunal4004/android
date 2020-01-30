package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class KotlinUtils {
    companion object {
        fun highlightTextInDesc(context: Context?, spannableTitle: SpannableString, searchTerm: String, tvDesc: TextView?, textIsClickable: Boolean = true) {
            var start = spannableTitle.indexOf(searchTerm)
            if (start == -1) {
                start = 0
            }

            val end = start + searchTerm.length
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Utils.makeCall(context, searchTerm)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            val typeface: Typeface? =
                    context?.let { ResourcesCompat.getFont(it, R.font.myriad_pro_semi_bold_otf) }
            if (textIsClickable)
                spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val dimenPix =
                    context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
            typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                    ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvDesc?.text = spannableTitle
            tvDesc?.movementMethod = LinkMovementMethod.getInstance()
            tvDesc?.highlightColor = Color.TRANSPARENT
        }

        fun setTransparentStatusBar(appCompatActivity: AppCompatActivity?) {
            if (Build.VERSION.SDK_INT in 19..20) {
                appCompatActivity?.setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
            }
            if (Build.VERSION.SDK_INT >= 19) {
                appCompatActivity?.window?.decorView?.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
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

        fun getBottomSheetBehaviorDefaultAnchoredHeight(): Int? {
            val activity = WoolworthsApplication.getInstance()?.currentActivity
            val height: Int? = activity?.resources?.displayMetrics?.heightPixels ?: 0
            return height?.div(3)?.plus(Utils.dp2px(activity, 18f)) ?: 0
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


        fun getStatusBarHeight(activity: Activity?): Int {
            var result = 0
            val resourceId: Int =
                    activity?.resources?.getIdentifier("status_bar_height", "dimen", "android")
                            ?: result
            if (resourceId > 0) {
                result = activity?.resources?.getDimensionPixelSize(resourceId) ?: result
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

        fun roundCornerDrawable(v: View, color: String?) {
            if (TextUtils.isEmpty(color)) return
            val paddingDp = (12 * v.context.resources.displayMetrics.density).toInt()
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.RECTANGLE
            shape.cornerRadii = floatArrayOf(paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat(), paddingDp.toFloat())
            shape.setColor(Color.parseColor(color))
            shape.setStroke(1, Color.parseColor(color))
            v.background = shape
        }

    }
}