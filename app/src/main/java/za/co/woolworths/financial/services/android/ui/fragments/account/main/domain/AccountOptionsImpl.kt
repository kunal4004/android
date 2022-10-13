package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain

import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountOptionsScreenUI
import javax.inject.Inject

interface IAccountOptions {
    suspend fun collectionTreatmentPlanItem(eligibilityPlan: EligibilityPlan): MutableList<AccountOptionsScreenUI>
    suspend fun balanceProtectionInsurance(): MutableList<AccountOptionsScreenUI>
    suspend fun isDebitOrderActive(): MutableList<AccountOptionsScreenUI>
    suspend fun paymentOptions(): MutableList<AccountOptionsScreenUI>
    suspend fun withdrawCashNow(): MutableList<AccountOptionsScreenUI>
    fun getItems(): MutableList<AccountOptionsScreenUI>
}

class AccountOptionsImpl @Inject constructor(
     val account: AccountProductLandingDao?,
     val bpi: BalanceProtectionInsuranceImpl
) : IAccountOptions, IBalanceProtectionInsurance by bpi {

    private var listOfAccountOptionsItem: MutableList<AccountOptionsScreenUI>

    init {
        listOfAccountOptionsItem = getItems()
    }

    enum class ListOptionsIndex(val number: Int) {
        INDEX_VIEW_TREATMENT_PLAN(number = 0),
        INDEX_SETUP_PAYMENT_PLAN(number = 1),
        INDEX_BALANCE_PROTECTION_INSURANCE(number = 2),
        INDEX_PAYMENT_OPTIONS(number = 3),
        INDEX_WITHDRAW_CASH_NOW(number = 4),
        INDEX_DEBIT_ORDER(number = 5)
    }

    override suspend fun collectionTreatmentPlanItem(eligibilityPlan: EligibilityPlan): MutableList<AccountOptionsScreenUI> {
        with(eligibilityPlan) {
            when (actionText) {
                ActionText.TAKE_UP_TREATMENT_PLAN.value -> {
                    val planType = AccountOptionsScreenUI.SetUpAPaymentPlan(
                        isVisible = true,
                        this
                    )
                    listOfAccountOptionsItem[ListOptionsIndex.INDEX_SETUP_PAYMENT_PLAN.number] =
                        planType
                }
                ActionText.VIEW_TREATMENT_PLAN.value -> {

                    val planType = AccountOptionsScreenUI.ViewTreatmentPlan(
                        true,
                        this
                    )
                    listOfAccountOptionsItem[ListOptionsIndex.INDEX_VIEW_TREATMENT_PLAN.number] = planType
                }
            }

            return listOfAccountOptionsItem
        }
    }

    override suspend fun balanceProtectionInsurance(): MutableList<AccountOptionsScreenUI> {
        val tag = balanceProtectionInsuranceTag()
        listOfAccountOptionsItem[ListOptionsIndex.INDEX_BALANCE_PROTECTION_INSURANCE.number] = tag
        return listOfAccountOptionsItem
    }

    override suspend fun isDebitOrderActive(): MutableList<AccountOptionsScreenUI> {
        val isActive = account?.product?.debitOrder?.debitOrderActive ?: false
        listOfAccountOptionsItem[ListOptionsIndex.INDEX_DEBIT_ORDER.number] =
            AccountOptionsScreenUI.DebitOrder(isActive)
        return listOfAccountOptionsItem
    }

    override suspend fun paymentOptions(): MutableList<AccountOptionsScreenUI> {
        listOfAccountOptionsItem[ListOptionsIndex.INDEX_PAYMENT_OPTIONS.number]=
            AccountOptionsScreenUI.PaymentOptionsScreenUI()
        return listOfAccountOptionsItem
    }

    override suspend fun withdrawCashNow(): MutableList<AccountOptionsScreenUI> {
        listOfAccountOptionsItem[ListOptionsIndex.INDEX_PAYMENT_OPTIONS.number]=
            AccountOptionsScreenUI.WithdrawCashNow(false)
        return listOfAccountOptionsItem
    }

    override fun getItems() = mutableListOf(
        AccountOptionsScreenUI.ViewTreatmentPlan(false),
        AccountOptionsScreenUI.SetUpAPaymentPlan(false),
        AccountOptionsScreenUI.BalanceProtectionInsurance(BpiInsuranceApplicationStatusType.NOT_COVERED),
        AccountOptionsScreenUI.PaymentOptionsScreenUI(false), // mandatory
        AccountOptionsScreenUI.WithdrawCashNow(false),   // Withdraw cash now is not applicable for store card
        AccountOptionsScreenUI.DebitOrder(false)
    )
}