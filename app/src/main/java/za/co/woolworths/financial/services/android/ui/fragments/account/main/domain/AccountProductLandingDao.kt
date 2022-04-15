package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import android.view.View
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.CreditCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.ProductLandingGroupCode
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IAccountProductLandingDao {
    val product : Account?
    fun getProductByProductGroupCode(): ProductLandingGroupCode
    fun getProductOfferingId():Int
    fun isProductInGoodStanding(): Boolean
    fun isProductChargedOff(): Boolean
    fun isUiVisible() : Int
    fun getTitleId() : Int
    fun getVisionAccountNumber(): String
    fun getProductGroupCode() : String
}

class AccountProductLandingDao @Inject constructor() : IAccountProductLandingDao {

    override val product: Account = SaveResponseDao.getValue(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD)

    override fun getProductByProductGroupCode(): ProductLandingGroupCode {
        return when (product.productGroupCode) {
            ProductLandingGroupCode.StoreCard().name -> ProductLandingGroupCode.StoreCard()
            ProductLandingGroupCode.PersonalLoan().name -> ProductLandingGroupCode.PersonalLoan()
            ProductLandingGroupCode.CreditCard().name -> when (product.accountNumberBin) {
                CreditCardType.BLACK_CARD.card -> ProductLandingGroupCode.BlackCreditCard()
                CreditCardType.SILVER_CARD.card -> ProductLandingGroupCode.SilverCreditCard()
                else -> ProductLandingGroupCode.GoldCreditCard()
            }
            else -> ProductLandingGroupCode.UnsupportedProductGroupCode
        }
    }

    override fun getProductOfferingId(): Int = product.productOfferingId

    override fun isProductInGoodStanding(): Boolean = product.productOfferingGoodStanding

    override fun isProductChargedOff(): Boolean = product.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)

    override fun isUiVisible(): Int = if (isProductInGoodStanding()) View.GONE else View.VISIBLE

    override fun getTitleId(): Int {
        return when (val productGroupCode = getProductByProductGroupCode()) {
            is ProductLandingGroupCode.PersonalLoan -> productGroupCode.title
            is ProductLandingGroupCode.StoreCard -> productGroupCode.title
            is ProductLandingGroupCode.BlackCreditCard -> productGroupCode.title
            is ProductLandingGroupCode.GoldCreditCard -> productGroupCode.title
            is ProductLandingGroupCode.SilverCreditCard -> productGroupCode.title
            else -> R.string.app_name
        }
    }

    override fun getVisionAccountNumber() = product.accountNumber ?: ""

    override fun getProductGroupCode(): String = product.productGroupCode ?: ""
}