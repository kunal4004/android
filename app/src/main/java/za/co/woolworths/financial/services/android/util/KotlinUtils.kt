package za.co.woolworths.financial.services.android.util

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

enum class LinkType { PHONE, EMAIL }

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
            if (textIsClickable)
                spannableTitle.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val dimenPix =
                    context?.resources?.getDimension(R.dimen.store_card_spannable_text_17_sp_bold)
            typeface?.style?.let { style -> spannableTitle.setSpan(StyleSpan(style), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) }
            spannableTitle.setSpan(AbsoluteSizeSpan(dimenPix?.toInt()
                    ?: 0), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableTitle
        }

        fun makeStringUnderlinedAndClickable(description: String, searchKeywordArray: Array<Triple<String, LinkType, String>>): Spannable {
            val spannableContent: Spannable = SpannableString(description)
            searchKeywordArray.forEach { items ->
                val searchTerm = items.first
                when (items.second) {
                    LinkType.PHONE -> {
                        val phoneNumber = items.third
                        val start = description.indexOf(searchTerm.first())
                        val end = description.lastIndexOf(searchTerm.last()) + 1
                        spannableContent.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                Utils.makeCall(phoneNumber)
                            }

                            override fun updateDrawState(textPaint: TextPaint) {
                                textPaint.isUnderlineText = true
                            }
                        }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        spannableContent.setSpan(UnderlineSpan(), start, end, 0)
                    }
                    LinkType.EMAIL -> {
                        val start = description.indexOf(searchTerm)
                        val end = description.lastIndexOf(searchTerm) + searchTerm.length
                        spannableContent.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                Utils.sendEmail(searchTerm)
                            }

                            override fun updateDrawState(textPaint: TextPaint) {
                                textPaint.isUnderlineText = true
                            }
                        }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        spannableContent.setSpan(UnderlineSpan(), start, end, 0)
                    }
                }
            }
            return spannableContent
        }
    }
}