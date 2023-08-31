package za.co.woolworths.financial.services.android.util.binding

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import za.co.woolworths.financial.services.android.ui.extension.makeCall

@BindingAdapter("android:setSpannableTtToSetDebitOrderTV")
fun TextView.setSpannableTtToSetDebitOrderTV(text: String) {
    val spannableString = SpannableString(text)
    val phoneNumber = extractPhoneNumber(text)!!
    val phoneNumberPosition = findContactPhoneNumberPositions(text, phoneNumber)!!
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            view.context.makeCall(phoneNumber)
        }
    }
    spannableString.apply {
        setSpan(
            ForegroundColorSpan(Color.BLACK),
            phoneNumberPosition.first, // start
            phoneNumberPosition.second, // end
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
        )
        setSpan(
            StyleSpan(Typeface.BOLD),
            phoneNumberPosition.first, // start
            phoneNumberPosition.second, // end
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
        )
        setSpan(
            UnderlineSpan(),
            phoneNumberPosition.first,
            phoneNumberPosition.second,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
        )
        setSpan(
            clickableSpan,
            phoneNumberPosition.first,
            phoneNumberPosition.second,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE,
        )
    }
    setText(spannableString, TextView.BufferType.SPANNABLE)
}

/**
 * Extracts the phone number in text [text]
 *
 * @author Adebayo Oloyede
 * @param [text] Text containing the phone number
 * @return [String] The extracted phone number
 * @since 9.12.0
 * */
private fun extractPhoneNumber(text: String): String? {
    val phoneNumberRegex = Regex("\\d{4} ?\\d{2} ?\\d{2} ?\\d{2}")
    val matchResult = phoneNumberRegex.find(text)
    return matchResult?.value
}

/**
 * Extracts the phone number in text [text]
 *
 * @author Adebayo Oloyede
 * @param [text] Text containing the phone number
 * @param [searchString] The phone number whose position within [text] is to be determined
 * @return [Pair]<[Int], [Int]> where [Pair.first] is the start position and [Pair.second] is the end position
 * @since 9.12.0
 * */
private fun findContactPhoneNumberPositions(text: String, searchString: String): Pair<Int, Int>? {
    val startIndex = text.indexOf(searchString)
    val endIndex = startIndex + searchString.length

    if (startIndex != -1) {
        return Pair(startIndex, endIndex)
    }

    return null
}
