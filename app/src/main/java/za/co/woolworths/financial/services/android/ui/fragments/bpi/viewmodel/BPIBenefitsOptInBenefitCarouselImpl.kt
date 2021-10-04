package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPIOptInBenefitCarouselInterface

class BPIBenefitsOptInBenefitCarouselImpl : BPIOptInBenefitCarouselInterface {

    override fun optInCarouselDataList(): MutableList<Triple<Int, Int, Int>> {
        return mutableListOf<Triple<Int, Int, Int>>().apply {
            add(
                Triple(
                    R.drawable.ic_bot,
                    R.string.bpi_opt_in_carousel_title_1,
                    R.string.bpi_opt_in_carousel_desc_1
                )
            )
            add(
                Triple(
                    R.drawable.ic_bot,
                    R.string.bpi_opt_in_carousel_title_2,
                    R.string.bpi_opt_in_carousel_desc_2
                )
            )
            add(
                Triple(
                    R.drawable.ic_bot,
                    R.string.bpi_opt_in_carousel_title_3,
                    R.string.bpi_opt_in_carousel_desc_3
                )
            )
        }
    }
}