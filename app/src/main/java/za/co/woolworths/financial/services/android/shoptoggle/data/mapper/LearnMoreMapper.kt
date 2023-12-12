package za.co.woolworths.financial.services.android.shoptoggle.data.mapper

import za.co.woolworths.financial.services.android.shoptoggle.data.dto.LearnMoreDto
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.LearnMore

fun LearnMoreDto.toDomain(): LearnMore {
    return LearnMore(
        icon = icon,
        title = title,
        description = description
    )
}