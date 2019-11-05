package za.co.woolworths.financial.services.android.util

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.*
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

            val typeface: Typeface? = context?.let { ResourcesCompat.getFont(it, R.font.myriad_pro_semi_bold_otf) }
            if (textIsClickable)
                spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            spannableTitle.setSpan(AbsoluteSizeSpan(50), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            tvDesc?.text = spannableTitle
            tvDesc?.movementMethod = LinkMovementMethod.getInstance()
            tvDesc?.highlightColor = Color.TRANSPARENT
        }

        // Luhn check algorithm
        fun validateCardNumberWithLuhnCheckAlgorithm(number: String): Boolean {
            var checksum = 0

            for (i in number.length - 1 downTo 0 step 2) {
                checksum += number[i] - '0'
            }
            for (i in number.length - 2 downTo 0 step 2) {
                val n: Int = (number[i] - '0') * 2
                checksum += if (n > 9) n - 9 else n
            }

            return checksum % 10 == 0
        }
    }
}