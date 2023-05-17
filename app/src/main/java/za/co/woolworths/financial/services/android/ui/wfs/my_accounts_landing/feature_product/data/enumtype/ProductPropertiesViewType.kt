package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.black_credit_card_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.gold_credit_card_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.personal_loan_account_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.pet_insurance
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.silver_credit_card_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.store_card_account_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.view_free_credit_report
import javax.annotation.concurrent.Immutable

enum class ProductPropertiesViewType {
    StoreCard,
    BlackCreditCard,
    GoldCreditCard,
    SilverCreditCard,
    PersonalLoan,
    PetInsurance,
    CreditReport;

    fun value() = when (this) {
        StoreCard -> ProductProperties(
            background = R.drawable.my_products_store_card_banner,
            productTitle = R.string.store_card_account_label,
            automationLocatorKey = store_card_account_key
        )
        BlackCreditCard -> ProductProperties(
            background = R.drawable.my_products_black_credit_card_banner,
            productTitle = R.string.credit_card_account_label,
            automationLocatorKey = black_credit_card_key
        )
        GoldCreditCard -> ProductProperties(
            background = R.drawable.my_products_gold_credit_card_banner,
            productTitle = R.string.credit_card_account_label,
            automationLocatorKey = gold_credit_card_key
        )
        SilverCreditCard -> ProductProperties(
            background = R.drawable.my_products_silver_card_banner,
            productTitle = R.string.credit_card_account_label,
            automationLocatorKey = silver_credit_card_key
        )
        PersonalLoan -> ProductProperties(
            background = R.drawable.my_products_personal_loan_banner,
            productTitle = R.string.my_offer_personal_loan_account_label,
            automationLocatorKey = personal_loan_account_key
        )
        PetInsurance -> ProductProperties(
            background = R.drawable.my_products_pet_insurance_banner,
            productTitle = R.string.wpet_care_classic_plan_label,
            availableProduct = R.string.wpet_care_classic_plan_label_policy_number,
            automationLocatorKey = pet_insurance
        )
        CreditReport -> ProductProperties(
            background = R.drawable.my_offer_credit_report_background,
            productTitle = R.string.wpet_care_classic_plan_label,
            availableProduct = R.string.wpet_care_classic_plan_label_policy_number,
            automationLocatorKey = view_free_credit_report
        )
    }
}

@Immutable
data class ProductProperties(
    @StringRes val viewButton: Int = R.string.view,
    @StringRes val retryButton: Int = R.string.retry_label,
    @StringRes val productTitle: Int = R.string.my_products,
    @StringRes val availableProduct: Int = R.string.acc_product_available,
    @StringRes val accountInArrearsLabel: Int = R.string.account_in_arrears,
    @DrawableRes val accountInArrearsIcon : Int = R.drawable.ic_account_in_arrears,
    @DrawableRes val background: Int = R.drawable.my_products_pet_insurance_banner,
    val automationLocatorKey: String = "")
