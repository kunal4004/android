package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local

import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse

interface IAccountDataClass{
    var accountData: Account?
    var storeCardsData: StoreCardsResponse?
}
class AccountDataClass() :IAccountDataClass{
    override var accountData: Account? = null
    override var storeCardsData: StoreCardsResponse? = null
}