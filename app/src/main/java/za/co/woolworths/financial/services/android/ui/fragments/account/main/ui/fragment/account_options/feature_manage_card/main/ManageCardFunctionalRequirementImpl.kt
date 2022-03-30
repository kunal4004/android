package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main

import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import javax.inject.Inject

interface IManageCardFunctionalRequirement {

}

class ManageCardFunctionalRequirementImpl
@Inject constructor() :
    IManageCardFunctionalRequirement {

    val storeCardsResponse: StoreCardsResponse by SaveResponseDao(SessionDao.KEY.STORE_CARE_RESPONSE_PAYLOAD, StoreCardsResponse::class.java)



}