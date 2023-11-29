package za.co.woolworths.financial.services.android.shoptoggle.domain.repository

import za.co.woolworths.financial.services.android.shoptoggle.data.dto.LearnMoreDto

interface LearnMoreRepository {
    fun getLearnMoreList(): List<LearnMoreDto>
}