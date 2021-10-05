package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import com.awfs.coordination.R

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class InsuranceLeadCarousel(
    @StringRes val titleResource: Int,
    @StringRes val buttonCaption: Int,
    @StringRes val descriptionResource: Int,
    @DrawableRes val imageResource: Int
) {
    SLIDE_1(
        R.string.bpi_opt_in_carousel_title_1,
        R.string.next,
        R.string.bpi_opt_in_carousel_desc_1,
        R.drawable.bpi_insurance_lead_carousel_slide_1_image
    ),
    SLIDE_2(
        R.string.bpi_opt_in_carousel_title_2,
        R.string.next,
        R.string.bpi_opt_in_carousel_desc_2,
        R.drawable.bpi_insurance_lead_carousel_slide_2_image
    ),
    SLIDE_3(
        R.string.bpi_opt_in_carousel_title_3,
        R.string.continueLabel,
        R.string.bpi_opt_in_carousel_desc_3,
        R.drawable.bpi_insurance_lead_carousel_slide_3_image
    )
}