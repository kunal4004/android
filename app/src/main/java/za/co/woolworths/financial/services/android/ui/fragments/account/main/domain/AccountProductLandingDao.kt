package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import android.view.View
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.DebitOrder
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.SaveResponseDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.CreditCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.ProductLandingGroupCode
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IAccountProductLandingDao {
    val product: Account?
    fun getProductByProductGroupCode(): ProductLandingGroupCode
    fun getProductOfferingId(): Int
    fun isProductInGoodStanding(): Boolean
    fun isProductChargedOff(): Boolean
    fun isUiVisible(): Int
    fun getTitleId(): Int
    fun getVisionAccountNumber(): String
    fun getProductGroupCode(): String
    fun getDebitOrder(): DebitOrder?
    fun getAccountInStringFormat(): String
    fun getApplyNowState(): ApplyNowState
}

class AccountProductLandingDao @Inject constructor() : IAccountProductLandingDao {

    override val product: Account? get()  = SaveResponseDao.getValue(SessionDao.KEY.ACCOUNT_PRODUCT_PAYLOAD)

    override fun getProductByProductGroupCode(): ProductLandingGroupCode {
        return when (product?.productGroupCode) {
            ProductLandingGroupCode.StoreCard().name -> ProductLandingGroupCode.StoreCard()
            ProductLandingGroupCode.PersonalLoan().name -> ProductLandingGroupCode.PersonalLoan()
            ProductLandingGroupCode.CreditCard().name -> when (product?.accountNumberBin) {
                CreditCardType.BLACK_CARD.card -> ProductLandingGroupCode.BlackCreditCard()
                CreditCardType.SILVER_CARD.card -> ProductLandingGroupCode.SilverCreditCard()
                else -> ProductLandingGroupCode.GoldCreditCard()
            }
            else -> ProductLandingGroupCode.UnsupportedProductGroupCode
        }
    }

    override fun getProductOfferingId(): Int = product?.productOfferingId ?: -1

    override fun isProductInGoodStanding(): Boolean = product?.productOfferingGoodStanding ?: false

    override fun isProductChargedOff(): Boolean =
        product?.productOfferingStatus.equals(Utils.ACCOUNT_CHARGED_OFF, ignoreCase = true)

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

    override fun getVisionAccountNumber() = product?.accountNumber ?: ""

    override fun getProductGroupCode(): String = product?.productGroupCode ?: ""

    override fun getDebitOrder(): DebitOrder? = product?.debitOrder

    override fun getAccountInStringFormat(): String = Gson().toJson(product)

    override fun getApplyNowState(): ApplyNowState {
        return when (product?.productGroupCode?.lowercase()) {
            AccountsProductGroupCode.STORE_CARD.groupCode.lowercase() -> ApplyNowState.STORE_CARD
            AccountsProductGroupCode.PERSONAL_LOAN.groupCode.lowercase() -> ApplyNowState.PERSONAL_LOAN
            else -> when (product?.accountNumberBin) {
                Utils.SILVER_CARD -> ApplyNowState.SILVER_CREDIT_CARD
                Utils.BLACK_CARD -> ApplyNowState.BLACK_CREDIT_CARD
                Utils.GOLD_CARD -> ApplyNowState.GOLD_CREDIT_CARD
                else -> ApplyNowState.STORE_CARD
            }
        }
    }
}