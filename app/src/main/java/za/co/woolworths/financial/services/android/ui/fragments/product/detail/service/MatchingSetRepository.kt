package za.co.woolworths.financial.services.android.ui.fragments.product.detail.service

import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 08/02/24.
 */
class MatchingSetRepository @Inject constructor(private val apiInterface: ApiInterface) :
    CoreDataSource(), ApiInterface by apiInterface {

}