package za.co.woolworths.financial.services.android.shoptoggle.domain.usecase

import za.co.woolworths.financial.services.android.shoptoggle.data.mapper.toDomain
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.LearnMore
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.LearnMoreRepository
import javax.inject.Inject

class LearnMoreUseCase @Inject constructor(
    private val learnMoreRepository: LearnMoreRepository,
) {

    operator fun invoke(): List<LearnMore> {
        return learnMoreRepository.getLearnMoreList().map { it.toDomain() }
    }

}