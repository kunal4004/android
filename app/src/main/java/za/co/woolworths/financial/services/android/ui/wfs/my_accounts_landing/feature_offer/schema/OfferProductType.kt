package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_offer.schema


import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.application_status
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.black_credit_card_apply_now_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.personal_loan_apply_now_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.pet_insurance
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.store_card_apply_now_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.view_free_credit_report
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.wolworths_store_card_apply_now_key
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.CommonItem
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.OfferClickEvent
import javax.annotation.concurrent.Immutable
enum class OfferProductType {
    PetInsurance,
    CreditCardApplyNow,
    BlackCreditCardApplyNow,
    StoreCardApplyNow,
    PersonalLoanApplyNow,
    ViewApplicationStatus,
    ViewFreeCreditReport;

    fun value(isAuthenticated : Boolean = true) = when (this) {
        PetInsurance -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_pet_insurance_background,
                title = R.string.my_offer_wpet_care_classic_plan_label,
                description = R.string.my_offer_wpet_care_classic_plan_desc,
                buttonId = R.string.my_offer_wpet_care_classic_plan_button_label,
                 isAnimationEnabled = true
            ),
            properties = OfferViewColors.Dark.color(),
            onClick = OfferClickEvent.PetInsurance,
            automationLocatorKey = pet_insurance,
            key = pet_insurance
        )
        BlackCreditCardApplyNow -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_credit_card_background,
                title = R.string.black_credit_card_title,
                description = R.string.blackCreditCard_desc,
                buttonId = R.string.apply_now
            ),
            properties = OfferViewColors.Dark.color(),
            automationLocatorKey = black_credit_card_apply_now_key,
            onClick = OfferClickEvent.BlackCreditCardApplyNow,
            key = black_credit_card_apply_now_key
        )

        CreditCardApplyNow -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_credit_card_background,
                title = R.string.black_credit_card_title,
                description = R.string.blackCreditCard_desc,
                buttonId = R.string.apply_now
            ),
            automationLocatorKey = black_credit_card_apply_now_key,
            properties = OfferViewColors.Dark.color(),
            onClick = OfferClickEvent.CreditCardApplyNow,
            key = wolworths_store_card_apply_now_key
        )
        StoreCardApplyNow -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_store_card_background,
                title = R.string.my_offer_woolworth_store_card_label,
                description = if (isAuthenticated) R.string.my_offer_woolworth_store_card_desc else R.string.my_offer_black_credit_card_dec ,
                buttonId = R.string.apply_now
            ),
            automationLocatorKey = store_card_apply_now_key,
            properties = OfferViewColors.Light.color(),
            onClick = OfferClickEvent.StoreCardApplyNow,
            key = store_card_apply_now_key
        )

        PersonalLoanApplyNow -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_personal_loan_apply_now_background,
                title = R.string.my_offer_personal_loan_account_label,
                description = R.string.my_offer_personal_loan_account_desc,
                buttonId = R.string.apply_now
            ),
            automationLocatorKey = personal_loan_apply_now_key,
            properties = OfferViewColors.Light.color(),
            onClick = OfferClickEvent.PersonalLoanApplyNow,
            key = personal_loan_apply_now_key
        )

        ViewFreeCreditReport -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_credit_report_background,
                title = R.string.my_credit_report_label,
                description = R.string.my_credit_status_sub_label,
                buttonId = R.string.get_started
            ),
            automationLocatorKey = view_free_credit_report,
            properties = OfferViewColors.Dark.color(),
            onClick = OfferClickEvent.ViewFreeCreditReport,
            key = view_free_credit_report
        )
        ViewApplicationStatus -> CommonItem.OfferItem(
            data = MyOfferData(
                image = R.drawable.my_offer_view_application_status_background,
                title = R.string.view_application_status,
                description = R.string.my_offer_view_application_status_description_sign_out,
                buttonId = R.string.my_offer_view_application_status_button
            ),
            properties = OfferViewColors.Dark.color(),
            onClick = OfferClickEvent.ViewApplicationStatus,
            automationLocatorKey = application_status,
            key = application_status
        )
    }
}

@Immutable
data class MyOfferData(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @StringRes val buttonId: Int,
    @DrawableRes val image: Int,
    val isAnimationEnabled : Boolean = false
)