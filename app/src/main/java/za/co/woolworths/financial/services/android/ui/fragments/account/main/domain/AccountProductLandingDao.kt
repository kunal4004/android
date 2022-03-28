package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import android.view.View
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.extension.fromJson
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.CreditCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.ProductLandingGroupCode
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.getSessionDaoValue
import javax.inject.Inject

interface IAccountProductLandingDao {
    val product : Account?
    fun saveAccount(product: String?)
    fun getAccountProduct(): Account?
    fun getProductByProductGroupCode(): ProductLandingGroupCode
    fun isProductInGoodStanding(): Boolean
    fun isProductChargedOff(): Boolean
    fun isUiVisible() : Int
    fun getTitleId() : Int
}

class AccountProductLandingDao @Inject constructor() : IAccountProductLandingDao {

    override val product : Account? = getAccountProduct()

    override fun saveAccount(product: String?) {
        product ?: return
        Utils.sessionDaoSave(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD, product)
    }

    override fun getAccountProduct(): Account? {
        val payload = getSessionDaoValue(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD)
        val gSon = Gson()
        return gSon.fromJson(payload)
    }

    override fun getProductByProductGroupCode(): ProductLandingGroupCode {
        val account = getAccountProduct()
        return when (account?.productGroupCode) {
            ProductLandingGroupCode.StoreCard().name -> ProductLandingGroupCode.StoreCard()
            ProductLandingGroupCode.PersonalLoan().name -> ProductLandingGroupCode.PersonalLoan()
            ProductLandingGroupCode.CreditCard().name -> when (account.accountNumberBin) {
                CreditCardType.BLACK_CARD.card -> ProductLandingGroupCode.BlackCreditCard()
                CreditCardType.SILVER_CARD.card -> ProductLandingGroupCode.SilverCreditCard()
                else -> ProductLandingGroupCode.GoldCreditCard()
            }
            else -> ProductLandingGroupCode.UnsupportedProductGroupCode
        }
    }

    override fun isProductInGoodStanding(): Boolean = product?.productOfferingGoodStanding == true

    override fun isProductChargedOff(): Boolean = product?.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)

    override fun isUiVisible(): Int = if (isProductInGoodStanding()) View.GONE else View.VISIBLE

    override fun getTitleId(): Int {
        return when (val productGroupCode = getProductByProductGroupCode()) {
            is ProductLandingGroupCode.PersonalLoan -> productGroupCode.title
            is ProductLandingGroupCode.StoreCard -> productGroupCode.title
            is ProductLandingGroupCode.BlackCreditCard -> productGroupCode.title
            is ProductLandingGroupCode.GoldCreditCard -> productGroupCode.title
            is ProductLandingGroupCode.SilverCreditCard -> productGroupCode.title
            is ProductLandingGroupCode.CreditCard,
            is ProductLandingGroupCode.UnsupportedProductGroupCode -> R.string.app_name
        }
    }
}