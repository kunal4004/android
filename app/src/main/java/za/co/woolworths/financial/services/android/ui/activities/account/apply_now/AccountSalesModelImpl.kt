package za.co.woolworths.financial.services.android.ui.activities.account.apply_now

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IAccountSalesContract
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.CardBenefit
import za.co.woolworths.financial.services.android.models.dto.account.CardCollection
import za.co.woolworths.financial.services.android.models.dto.account.CardQualifyCriteria
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.CardHeader
import za.co.woolworths.financial.services.android.models.dto.account.MoreBenefit

class AccountSalesModelImpl : IAccountSalesContract.AccountSalesModel {

    private val calendarIcon = R.drawable.icon_calendar
    private val cardsIcon = R.drawable.icon_cards
    private val fuelIcon = R.drawable.icon_fuel
    private val medicalIcon = R.drawable.icon_medical
    private val mobileIcon = R.drawable.icon_mobile
    private val moneyIcon = R.drawable.icon_money
    private val profileEditIcon = R.drawable.icon_profile_edit
    private val rewardIcon = R.drawable.icon_reward
    private val shoppingIcon = R.drawable.icon_shopping
    private val smileIcon = R.drawable.icon_smile
    private val trolleyIcon = R.drawable.icon_trolley
    private val voucherIcon = R.drawable.icon_voucher

    private val automaticRewardIcon = R.drawable.icon_automatic_rewards
    private val callCircleIcon = R.drawable.icon_call_circle
    private val cardCircleIcon = R.drawable.icon_card_circle
    private val coffeeCircleIcon = R.drawable.icon_coffee_circle
    private val deliveryCircleIcon = R.drawable.icon_delivery_circle
    private val easyToManageIcon = R.drawable.icon_easy_to_manage
    private val insuranceCircleIcon = R.drawable.icon_insurance_circle
    private val makeADifferenceIcon = R.drawable.icon_make_a_difference
    private val iconPayCircleIcon = R.drawable.icon_pay_circle
    private val yourInControlIcon = R.drawable.icon_youre_in_control
    private val voucherCircleIcon = R.drawable.icon_voucher_circle
    private val documentCircleIcon = R.drawable.icon_document_circle
    private val moneyCircleIcon = R.drawable.icon_money_circle

    private var storeCardHeaderDrawable =
            mutableListOf(
                    R.drawable.store_card_background,
                    R.drawable.store_card_front,
                    R.drawable.store_card_back)

    private var goldCreditCardHeaderDrawable =
            mutableListOf(
                    R.drawable.gold_credit_card_background,
                    R.drawable.gold_credit_card_front,
                    R.drawable.gold_credit_card_back)

    private var blackCreditCardHeaderDrawable =
            mutableListOf(
                    R.drawable.black_credit_card_background,
                    R.drawable.black_credit_card_front,
                    R.drawable.black_credit_card_back)

    private var personalLoanHeaderDrawable =
            mutableListOf(
                    R.drawable.personal_loan_background,
                    R.drawable.personal_loan_front,
                    R.drawable.personal_loan_back)

    private val storeCardHeader =
            CardHeader(getString(R.string.store_card_title), getString(R.string.store_card_desc), storeCardHeaderDrawable)

    private val goldCreditCardHeader =
            CardHeader(getString(R.string.gold_credit_card_title), getString(R.string.goldCreditCard_desc), goldCreditCardHeaderDrawable)

    private val blackCreditCardHeader =
            CardHeader(getString(R.string.black_credit_card_title), getString(R.string.blackCreditCard_desc), blackCreditCardHeaderDrawable)

    private val personalLoanHeader =
            CardHeader(getString(R.string.personal_loan_card_title), getString(R.string.personalLoanCard_desc), personalLoanHeaderDrawable)

    private val storeCardCardBenefits =
            mutableListOf(CardBenefit(automaticRewardIcon, getString(R.string.storeCardBenefits_row_1_title), getString(R.string.storeCardBenefits_row_1_desc), getString(R.string.cardBenefits_title)),
                    CardBenefit(makeADifferenceIcon, getString(R.string.storeCardBenefits_row_2_title), getString(R.string.storeCardBenefits_row_2_desc)),
                    CardBenefit(yourInControlIcon, getString(R.string.storeCardBenefits_row_3_title), getString(R.string.storeCardBenefits_row_3_desc)),
                    CardBenefit(easyToManageIcon, getString(R.string.storeCardBenefits_row_4_title), getString(R.string.storeCardBenefits_row_4_desc)))


    private val goldCardCreditCardBenefits =
            mutableListOf(CardBenefit(voucherCircleIcon, getString(R.string.goldCreditCardBenefits_row_1_title), getString(R.string.goldCreditCardBenefits_row_1_desc), getString(R.string.cardBenefits_title)),
                    CardBenefit(moneyCircleIcon, getString(R.string.goldCreditCardBenefits_row_2_title), getString(R.string.goldCreditCardBenefits_row_2_desc)),
                    CardBenefit(insuranceCircleIcon, getString(R.string.goldCreditCardBenefits_row_3_title), getString(R.string.goldCreditCardBenefits_row_3_desc)),
                    CardBenefit(cardCircleIcon, getString(R.string.goldCreditCardBenefits_row_4_title), getString(R.string.goldCreditCardBenefits_row_4_desc)))

    private val blackCardCreditCardBenefits =
            mutableListOf(CardBenefit(voucherCircleIcon, getString(R.string.blackCreditCardBenefits_row_1_title), getString(R.string.blackCreditCardBenefits_row_1_desc), getString(R.string.cardBenefits_title)),
                    CardBenefit(moneyCircleIcon, getString(R.string.blackCreditCardBenefits_row_2_title), getString(R.string.blackCreditCardBenefits_row_2_desc)),
                    CardBenefit(coffeeCircleIcon, getString(R.string.blackCreditCardBenefits_row_3_title), getString(R.string.blackCreditCardBenefits_row_3_desc)),
                    CardBenefit(deliveryCircleIcon, getString(R.string.blackCreditCardBenefits_row_4_title), getString(R.string.blackCreditCardBenefits_row_4_desc)))

    private val personalLoanCardBenefits =
            mutableListOf(CardBenefit(moneyCircleIcon, getString(R.string.personalLoanBenefits_row_1_title), getString(R.string.personalLoanBenefits_row_1_desc), getString(R.string.personalLoanBenefits_title)),
                    CardBenefit(callCircleIcon, getString(R.string.personalLoanBenefits_row_2_title), getString(R.string.personalLoanBenefits_row_2_desc)),
                    CardBenefit(iconPayCircleIcon, getString(R.string.personalLoanBenefits_row_3_title), getString(R.string.personalLoanBenefits_row_3_desc)),
                    CardBenefit(documentCircleIcon, getString(R.string.personalLoanBenefits_row_4_title), getString(R.string.personalLoanBenefits_row_4_desc)))

    private val storeCardMoreBenefits =
            mutableListOf(
                    MoreBenefit(shoppingIcon, getString(R.string.storeCardMoreBenefits_row_1_title), getString(R.string.storeCardMoreBenefits_row_1_desc)),
                    MoreBenefit(calendarIcon, getString(R.string.storeCardMoreBenefits_row_2_title), getString(R.string.storeCardMoreBenefits_row_2_desc)),
                    MoreBenefit(cardsIcon, getString(R.string.storeCardMoreBenefits_row_3_title), getString(R.string.storeCardMoreBenefits_row_3_desc)),
                    MoreBenefit(smileIcon, getString(R.string.storeCardMoreBenefits_row_4_title), getString(R.string.storeCardMoreBenefits_row_4_desc)),
                    MoreBenefit(mobileIcon, getString(R.string.storeCardMoreBenefits_row_5_title), getString(R.string.storeCardMoreBenefits_row_5_desc)))

    private val goldCreditCardMoreBenefits =
            mutableListOf(
                    MoreBenefit(rewardIcon, getString(R.string.goldCreditCardMoreBenefits_row_1_title), getString(R.string.goldCreditCardMoreBenefits_row_1_desc)),
                    MoreBenefit(voucherIcon, getString(R.string.goldCreditCardMoreBenefits_row_2_title), getString(R.string.goldCreditCardMoreBenefits_row_2_desc)),
                    MoreBenefit(fuelIcon, getString(R.string.goldCreditCardMoreBenefits_row_3_title), getString(R.string.goldCreditCardMoreBenefits_row_3_desc)),
                    MoreBenefit(smileIcon, getString(R.string.goldCreditCardMoreBenefits_row_4_title), getString(R.string.goldCreditCardMoreBenefits_row_4_desc)),
                    MoreBenefit(calendarIcon, getString(R.string.goldCreditCardMoreBenefits_row_5_title), getString(R.string.goldCreditCardMoreBenefits_row_5_desc)),
                    MoreBenefit(cardsIcon, getString(R.string.goldCreditCardMoreBenefits_row_6_title), getString(R.string.goldCreditCardMoreBenefits_row_6_desc)),
                    MoreBenefit(medicalIcon, getString(R.string.goldCreditCardMoreBenefits_row_7_title), getString(R.string.goldCreditCardMoreBenefits_row_7_desc)))

    private val blackCreditCardMoreBenefits =
            mutableListOf(
                    MoreBenefit(trolleyIcon, getString(R.string.blackCreditCardMoreBenefits_row_1_title), getString(R.string.blackCreditCardMoreBenefits_row_1_desc)),
                    MoreBenefit(moneyIcon, getString(R.string.blackCreditCardMoreBenefits_row_2_title), getString(R.string.blackCreditCardMoreBenefits_row_2_desc)),
                    MoreBenefit(moneyIcon, getString(R.string.blackCreditCardMoreBenefits_row_3_title), getString(R.string.blackCreditCardMoreBenefits_row_3_desc)),
                    MoreBenefit(fuelIcon, getString(R.string.blackCreditCardMoreBenefits_row_4_title), getString(R.string.blackCreditCardMoreBenefits_row_4_desc)),
                    MoreBenefit(rewardIcon, getString(R.string.blackCreditCardMoreBenefits_row_5_title), getString(R.string.blackCreditCardMoreBenefits_row_5_desc)),
                    MoreBenefit(smileIcon, getString(R.string.blackCreditCardMoreBenefits_row_6_title), getString(R.string.blackCreditCardMoreBenefits_row_6_desc)),
                    MoreBenefit(calendarIcon, getString(R.string.blackCreditCardMoreBenefits_row_7_title), getString(R.string.blackCreditCardMoreBenefits_row_7_desc)),
                    MoreBenefit(cardsIcon, getString(R.string.blackCreditCardMoreBenefits_row_8_title), getString(R.string.blackCreditCardMoreBenefits_row_8_desc)),
                    MoreBenefit(medicalIcon, getString(R.string.blackCreditCardMoreBenefits_row_9_title), getString(R.string.blackCreditCardMoreBenefits_row_9_desc)))

    private val personalLoanMoreBenefits =
            mutableListOf(
                    MoreBenefit(smileIcon, getString(R.string.personalLoanMoreBenefits_row_1_title), getString(R.string.personalLoanMoreBenefits_row_1_desc)),
                    MoreBenefit(moneyIcon, getString(R.string.personalLoanMoreBenefits_row_2_title), getString(R.string.personalLoanMoreBenefits_row_2_desc)),
                    MoreBenefit(mobileIcon, getString(R.string.personalLoanMoreBenefits_row_3_title), getString(R.string.personalLoanMoreBenefits_row_3_desc)))


    private val storeCardQualifyCriteria =
            mutableListOf(CardQualifyCriteria(getString(R.string.storeCardQualifyCriteria_row_1)),
                    CardQualifyCriteria(getString(R.string.storeCardQualifyCriteria_row_2)),
                    CardQualifyCriteria(getString(R.string.storeCardQualifyCriteria_row_3)))

    private val goldCreditCardQualifyCriteria =
            mutableListOf(
                    CardQualifyCriteria(getString(R.string.goldCreditCardQualify_row_1)),
                    CardQualifyCriteria(getString(R.string.goldCreditCardQualify_row_2)),
                    CardQualifyCriteria(getString(R.string.goldCreditCardQualify_row_3)))

    private val blackCreditCardQualifyCriteria =
            mutableListOf(
                    CardQualifyCriteria(getString(R.string.blackCreditCardQualify_row_1)),
                    CardQualifyCriteria(getString(R.string.blackCreditCardQualify_row_2)),
                    CardQualifyCriteria(getString(R.string.blackCreditCardQualify_row_3)))

    private val personalLoanQualifyCriteria =
            mutableListOf(
                    CardQualifyCriteria(getString(R.string.personalLoanQualifyingCriteria_row_1)),
                    CardQualifyCriteria(getString(R.string.personalLoanQualifyingCriteria_row_2)),
                    CardQualifyCriteria(getString(R.string.personalLoanQualifyingCriteria_row_3)),
                    CardQualifyCriteria(getString(R.string.personalLoanQualifyingCriteria_row_4)),
                    CardQualifyCriteria(getString(R.string.personalLoanQualifyingCriteria_row_5)))

    private val storeCardCardCollection =
            mutableListOf(
                    CardCollection(getString(R.string.storeCardCardCollection_row_1)),
                    CardCollection(getString(R.string.storeCardCardCollection_row_2)))

    private val goldCreditCardCardCollection =
            mutableListOf(
                    CardCollection(getString(R.string.goldCreditCardCollection_row_1)),
                    CardCollection(getString(R.string.goldCreditCardCollection_row_2)),
                    CardCollection(getString(R.string.goldCreditCardCollection_row_3)))

    private val blackCreditCardCardCollection =
            mutableListOf(CardCollection(getString(R.string.blackCreditCardCollection_row_1)),
                    CardCollection(getString(R.string.blackCreditCardCollection_row_2)),
                    CardCollection(getString(R.string.blackCreditCardCollection_row_3)))

    private fun getGoldCreditCardAccountSalesItem() = AccountSales(
            goldCreditCardHeader,
            goldCardCreditCardBenefits,
            goldCreditCardMoreBenefits,
            goldCreditCardQualifyCriteria,
            goldCreditCardCardCollection)

    private fun getBlackCreditCardAccountSalesItem() = AccountSales(
            blackCreditCardHeader,
            blackCardCreditCardBenefits,
            blackCreditCardMoreBenefits,
            blackCreditCardQualifyCriteria,
            blackCreditCardCardCollection)

    override fun getStoreCard() = AccountSales(
            storeCardHeader,
            storeCardCardBenefits,
            storeCardMoreBenefits,
            storeCardQualifyCriteria,
            storeCardCardCollection)

    override fun getPersonalLoan() = AccountSales(
            personalLoanHeader,
            personalLoanCardBenefits,
            personalLoanMoreBenefits,
            personalLoanQualifyCriteria)

    override fun getCreditCard() = mutableListOf(
            getGoldCreditCardAccountSalesItem(),
            getBlackCreditCardAccountSalesItem())

    private fun getString(stringId: Int): String? = (WoolworthsApplication.getInstance()?.currentActivity as? FragmentActivity)?.resources?.getString(stringId)
}