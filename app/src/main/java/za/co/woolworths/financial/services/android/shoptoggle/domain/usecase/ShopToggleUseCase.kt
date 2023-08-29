package za.co.woolworths.financial.services.android.shoptoggle.domain.usecase

import za.co.woolworths.financial.services.android.shoptoggle.data.mapper.toDomain
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.ShopToggleRepository
import javax.inject.Inject

class ShopToggleUseCase @Inject constructor(

    private val shopToggleRepository: ShopToggleRepository,

    ) {
    operator fun invoke(): List<ToggleModel> {
        return shopToggleRepository.getShopToggleList().map { it.toDomain() }
    }

}