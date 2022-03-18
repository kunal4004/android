package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.awfs.coordination.R

sealed class ProductLandingGroupCode {
    data class PersonalLoan(
        val name: String = AccountCardType.PERSONAL_LOAN.type,
        @DrawableRes val background: Int = R.drawable.personal_loan_background,
        @StringRes val title: Int = R.string.personal_loan_card_title
    ) : ProductLandingGroupCode()

    data class StoreCard(
        val name: String = AccountCardType.STORE_CARD.type,
        @DrawableRes val background: Int = R.drawable.store_card_background,
        @StringRes val title: Int = R.string.store_card_title
    ) : ProductLandingGroupCode()

    data class CreditCard(
        val name: String = AccountCardType.CREDIT_CARD.type,
        @DrawableRes val background: Int = R.drawable.store_card_background,
        @StringRes val title: Int = R.string.store_card_title,
        val type: CreditCardType = CreditCardType.GOLD_CARD
    ) : ProductLandingGroupCode()

    data class BlackCreditCard(
        @DrawableRes val background: Int = R.drawable.black_credit_card_background,
        @StringRes val title: Int = R.string.black_credit_card_title
    ) : ProductLandingGroupCode()

    data class SilverCreditCard(
        @DrawableRes val background: Int = R.drawable.silver_credit_card_background,
        @StringRes val title: Int = R.string.silver_credit_card_title
    ) : ProductLandingGroupCode()

    data class GoldCreditCard(
        @DrawableRes val background: Int = R.drawable.gold_credit_card_background,
        @StringRes val title: Int = R.string.gold_credit_card_title
    ) : ProductLandingGroupCode()

    object UnsupportedProductGroupCode : ProductLandingGroupCode()
}

enum class AccountCardType(val type: String) {
    STORE_CARD("SC"),
    PERSONAL_LOAN("PL"),
    CREDIT_CARD("CC")
}

enum class CreditCardType(val card: String) {
    BLACK_CARD("410375"),
    SILVER_CARD("400154"),
    GOLD_CARD("410374")
}
