package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import com.awfs.coordination.R

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.BpiSlideText

val slidesText = WoolworthsApplication.getBalanceProtectionInsuranceObject().coverage

fun getTitle(slide: BpiSlideText?): String {
    return slide?.title ?: ""
}

fun getDescription(slide: BpiSlideText?): Spannable {
    if(slide?.description != null && slide.descriptionBoldParts.isNotEmpty()){
        val description: Spannable = SpannableString(slide.description)
        slide.descriptionBoldParts.forEach {
            val start: Int = description.indexOf(it)
            val end: Int = start + it.length
            description.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return description
    }

    return SpannableString(slide?.description)
}

enum class InsuranceLeadCarousel(
    val title: String?,
    @StringRes val buttonCaption: Int,
    val description: Spannable?,
    @DrawableRes val imageResource: Int
) {
    SLIDE_1(
        getTitle(slidesText?.slide1),
        R.string.next,
        getDescription(slidesText?.slide1),
        R.drawable.bpi_insurance_lead_carousel_slide_1_image
    ),
    SLIDE_2(
        getTitle(slidesText?.slide2),
        R.string.next,
        getDescription(slidesText?.slide2),
        R.drawable.bpi_insurance_lead_carousel_slide_2_image
    ),
    SLIDE_3(
        getTitle(slidesText?.slide3),
        R.string.continueLabel,
        getDescription(slidesText?.slide3),
        R.drawable.bpi_insurance_lead_carousel_slide_3_image
    )
}