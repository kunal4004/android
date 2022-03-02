package za.co.woolworths.financial.services.android.ui.fragments.account.main.usecase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.extension.fromJson
import za.co.woolworths.financial.services.android.ui.fragments.account.main.sealing.CreditCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.sealing.ProductLandingGroupCode
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.getSessionDaoValue
import javax.inject.Inject

interface IAccountProductLandingDao {
    val account: LiveData<Account>
    fun saveAccount(product: String?)
    fun getAccountProduct(): Account?
    fun getProductGroupCode(): ProductLandingGroupCode
}

class AccountProductLandingDao @Inject constructor() : IAccountProductLandingDao {

    override val account by lazy { MutableLiveData<Account>() }
    override fun saveAccount(product: String?) {
        product ?: return
        Utils.sessionDaoSave(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD, product)
    }

    override fun getAccountProduct(): Account? {
        val payload = getSessionDaoValue(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD)
        val gSon = Gson()
        val product = gSon.fromJson<Account?>(payload)
        account.postValue(product)
        return product
    }

    override fun getProductGroupCode(): ProductLandingGroupCode {
        val payload = account.value
        return when (payload?.productGroupCode) {
            ProductLandingGroupCode.StoreCard().name -> ProductLandingGroupCode.StoreCard()
            ProductLandingGroupCode.PersonalLoan().name -> ProductLandingGroupCode.PersonalLoan()
            ProductLandingGroupCode.CreditCard().name -> when (payload.accountNumberBin) {
                CreditCardType.BLACK_CARD.card -> ProductLandingGroupCode.BlackCreditCard
                CreditCardType.SILVER_CARD.card -> ProductLandingGroupCode.SilverCard
                else -> ProductLandingGroupCode.GoldCreditCard
            }
            else -> ProductLandingGroupCode.UnsupportedProductGroupCode
        }
    }
}