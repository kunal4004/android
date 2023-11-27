package za.co.woolworths.financial.services.android.shoptoggle.data.repository

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.shoptoggle.data.dto.LearnMoreDto
import za.co.woolworths.financial.services.android.shoptoggle.domain.repository.LearnMoreRepository
import javax.inject.Inject

class LearnMoreRepositoryImpl @Inject constructor(
    private val resourcesProvider: ResourcesProvider,
) : LearnMoreRepository {
    override fun getLearnMoreList(): List<LearnMoreDto> {

        return listOf(
            LearnMoreDto(
                icon = R.drawable.ic_delivery_truck_black_24,
                title = resourcesProvider.getString(R.string.standard_delivery),
                description = resourcesProvider.getString(R.string.learn_more_description)
            ),
            LearnMoreDto(
                icon = R.drawable.ic_bike_black_24,
                title = resourcesProvider.getString(R.string.dash_delivery),
                description = resourcesProvider.getString(R.string.learn_more_dash_desc)
            ),
            LearnMoreDto(
                icon = R.drawable.ic_basket_black_24,
                title = resourcesProvider.getString(R.string.click_and_collect),
                description = resourcesProvider.getString(R.string.learn_more_click_desc)
            )

        )
    }
}