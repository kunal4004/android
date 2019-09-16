package za.co.woolworths.financial.services.android.util

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R

class KotlinUtils {
    companion object {
        fun contactCustomerCare(activity: Activity?, spannableTitle: SpannableString, phoneNumber: String, tvDesc: TextView?, descIsClickable: Boolean = true) {
            val start = spannableTitle.indexOf(phoneNumber)
            val end = start + phoneNumber.length
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Utils.makeCall(activity, phoneNumber)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            val typeface: Typeface? = activity?.let { ResourcesCompat.getFont(it, R.font.myriad_pro_semi_bold_otf) }
            if (descIsClickable)
                spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            spannableTitle.setSpan(AbsoluteSizeSpan(50), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvDesc?.text = spannableTitle
            tvDesc?.movementMethod = LinkMovementMethod.getInstance()
            tvDesc?.highlightColor = Color.TRANSPARENT
        }
    }
}