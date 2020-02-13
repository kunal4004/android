package za.co.woolworths.financial.services.android.util

import android.content.Context
import android.graphics.Typeface
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R

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
                when (val linkType = items.second) {
                    LinkType.PHONE -> {
                        val phoneNumber = items.third
                        val firstIndexOfSearchKeyword = description.indexOf(searchTerm)
                        val lastIndexOfSearchKeyword = description.lastIndexOf(searchTerm).plus(searchTerm.length)
                        makeStringsUnderline(makeStringsClickable(firstIndexOfSearchKeyword, lastIndexOfSearchKeyword, phoneNumber, linkType, spannableContent), firstIndexOfSearchKeyword, lastIndexOfSearchKeyword)
                    }
                    LinkType.EMAIL -> {
                        val firstIndexOfSearchKeyword = description.indexOf(searchTerm)
                        val lastIndexOfSearchKeyword = description.lastIndexOf(searchTerm) + searchTerm.length
                        makeStringsUnderline(makeStringsClickable(firstIndexOfSearchKeyword, lastIndexOfSearchKeyword, searchTerm, linkType, spannableContent), firstIndexOfSearchKeyword, lastIndexOfSearchKeyword)
                    }
                }
            }
            return spannableContent
        }

        private fun makeStringsClickable(firstIndexOfSearchKeyword: Int, lastIndexOfSearchKeyword: Int, term: String, linkType: LinkType, spannableContent: Spannable): Spannable {
            spannableContent.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    when (linkType) {
                        LinkType.PHONE -> Utils.makeCall(term)
                        LinkType.EMAIL -> Utils.sendEmail(term)
                    }
                }

                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.isUnderlineText = true
                }
            }, firstIndexOfSearchKeyword, lastIndexOfSearchKeyword, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            return spannableContent
        }

        private fun makeStringsUnderline(spannableContent: Spannable, firstIndexOfSearchKeyword: Int, lastIndexOfSearchKeyword: Int) {
            spannableContent.setSpan(UnderlineSpan(), firstIndexOfSearchKeyword, lastIndexOfSearchKeyword, 0)
        }
    }
}