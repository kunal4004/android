package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer

import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.data.IOfferSectionModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.data.OfferSectionModel
import javax.inject.Inject

class OfferSectionUseCase @Inject constructor(
    private val offerSectionModel: OfferSectionModel
) : IOfferSectionModel by offerSectionModel