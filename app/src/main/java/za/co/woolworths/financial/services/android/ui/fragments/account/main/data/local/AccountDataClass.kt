package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.local

import za.co.woolworths.financial.services.android.models.dto.Account

interface IAccountDataClass{
    var accountData: Account?
}
class AccountDataClass() :IAccountDataClass{
    override var accountData: Account? = null
}