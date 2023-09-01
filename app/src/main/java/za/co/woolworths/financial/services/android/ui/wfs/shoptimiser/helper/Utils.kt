package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ProductOnDisplay
import za.co.woolworths.financial.services.android.ui.wfs.theme.Color444444
import za.co.woolworths.financial.services.android.ui.wfs.theme.HyperTextlinkBlue

/**
 * Capitalizes the first letter of each word in the input string and returns the modified string.
 * @param input The input string to be capitalized.
 * @return The input string with each word's first letter capitalized.
 */
fun wordsToTitleCase(input: String?): String? {
    return input?.split(" ")
       ?.joinToString(" ") { it
            .lowercase()
            .replaceFirstChar(Char::titlecase)
        }
}

/**
 * Converts plain text into an AnnotatedString with formatted styles and hyperlinks.
 * @param sentence The plain text sentence to be converted.
 * @param wordToFormatList A list of words to be formatted with bold style.
 * @param descriptionHyperlinkPart The text portion to be underlined as a hyperlink.
 * @return An AnnotatedString with applied styles and hyperlinks.
 */
fun convertTextToAnnotationString(
    sentence: String?,
    wordToFormatList: MutableList<String>?,
    descriptionHyperlinkPart: String? = null
): AnnotatedString {
    return buildAnnotatedString {
        // Append the plain sentence
        append(sentence)

        // Format words in wordToFormatList with bold style
        for (word in wordToFormatList ?: mutableListOf()) {
            val startIndex = sentence?.indexOf(word.trim()) ?: 0
            if (startIndex != -1) {
                val endIndex = (startIndex + word.trim().length)
                addStyle(
                    style = SpanStyle(color = Color444444,
                        fontWeight = FontWeight.Bold),
                    start = startIndex,
                    end = endIndex
                )
            }
        }
        // Add underline style for the hyperlink portion
        descriptionHyperlinkPart?.let { underlinedText ->
            val underlinedStartPosition = sentence?.indexOf(underlinedText) ?: 0
            val underlinedEndPosition = (underlinedStartPosition + underlinedText.length)
            addStyle(
                style = SpanStyle(
                    color  = HyperTextlinkBlue,
                    fontWeight = FontWeight.W400,
                    textDecoration = TextDecoration.Underline),
                start = underlinedStartPosition,
                end = underlinedEndPosition
            )
        }
    }
}

/**
 * Sorts a map by a custom order of keys and returns the sorted map.
 * @param originalMap The original map to be sorted.
 * @param keyOrder The list of keys specifying the custom order.
 * @return A new map sorted according to the custom key order.
 */
 fun sortMapByCustomKeyOrder(originalMap: MutableMap<String, ProductOnDisplay>, keyOrder: MutableList<String>): MutableMap<String, ProductOnDisplay> {
    val sortedEntries = originalMap.entries.sortedBy { keyOrder.indexOf(it.key) }
    val sortedMap = mutableMapOf<String, ProductOnDisplay>()
    sortedEntries.forEach { (key, value) -> sortedMap[key] = value }
    return sortedMap
}