package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.annotation.SuppressLint
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOfferingStatus
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountInArrears
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.AccountInDelinquency
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.DateFormatter
import za.co.woolworths.financial.services.android.util.Utils

interface IViewTreatmentPlan {
    val productOfferingStatus: ProductOfferingStatus
    fun getPlanType(): TreatmentPlanType
    fun isViewTreatmentPlan(): Boolean
    fun isCreditCardProduct(): Boolean
    fun getTitleAndDescription(): Pair<Int, String?>
    fun getViewTreatmentPlanDescription(applyNowState: ApplyNowState?): String?
    fun getPlanDescription(applyNowState: ApplyNowState?): Int
    fun getString(@StringRes stringId: Int, value: String): String
    fun isViewPaymentOptionsButtonVisible(): Int
    fun isMakePaymentButtonVisible(): Int
    fun isTreatmentPlanTakeUp(): Boolean
    fun makePaymentPlanButtonLabel(): String
    fun cannotAffordPaymentFirebaseEvent(): Pair<String, String>
    fun isCannotAffordPaymentButtonVisible(): Int
    fun isAccountChargedOff() : Boolean
    fun isViewElitePlanEnabled(eligibilityPlan: EligibilityPlan?) : Boolean
    fun isElitePlanEnabled(eligibilityPlan: EligibilityPlan?) : Boolean
    fun getPopupData(eligibilityPlan: EligibilityPlan?): DialogData
}

enum class TreatmentPlanType {
    VIEW, TAKE_UP, ELITE, NONE
}

class ViewTreatmentPlanImpl (
    private val eligibilityPlan: EligibilityPlan?,
    private val account: Account?,
    private val applyNowState: ApplyNowState?
) : IViewTreatmentPlan {

    override val productOfferingStatus: ProductOfferingStatus
        get() = ProductOfferingStatus(account)

    override fun getPlanType() = when (eligibilityPlan?.actionText) {
        ActionText.TAKE_UP_TREATMENT_PLAN.value -> TreatmentPlanType.TAKE_UP
        ActionText.VIEW_TREATMENT_PLAN.value -> TreatmentPlanType.VIEW
        ActionText.VIEW_ELITE_PLAN.value -> TreatmentPlanType.ELITE
        else -> TreatmentPlanType.NONE
    }

    override fun isViewTreatmentPlan(): Boolean =
        eligibilityPlan?.actionText == ActionText.VIEW_TREATMENT_PLAN.value ||
                eligibilityPlan?.actionText == ActionText.VIEW_ELITE_PLAN.value

    override fun isCreditCardProduct(): Boolean =
        applyNowState != ApplyNowState.STORE_CARD || applyNowState != ApplyNowState.PERSONAL_LOAN

    @SuppressLint("VisibleForTests")
    override fun getTitleAndDescription(): Pair<Int, String?> {
        val amountOverdue = "R ${getAmountOverdue()}"
        return when (isAccountChargedOff()) {
            true -> {
                when (isCreditCardProduct() && (productOfferingStatus.isViewTreatmentPlanSupported()
                        || productOfferingStatus.isTakeUpTreatmentPlanJourneyEnabled())) {
                    true -> when(getPlanType()) {
                        TreatmentPlanType.VIEW, TreatmentPlanType.ELITE -> R.string.account_in_recovery_label to getViewTreatmentPlanDescription(
                            applyNowState
                        )
                        else -> R.string.remove_block_on_collection_dialog_title to bindString(R.string.remove_block_on_collection_dialog_desc)
                    }
                    false -> R.string.remove_block_on_collection_dialog_title to bindString(R.string.remove_block_on_collection_dialog_desc)
                }
            }
            false -> {
                when (productOfferingStatus.isViewTreatmentPlanSupported()
                        || productOfferingStatus.isTakeUpTreatmentPlanJourneyEnabled()) {
                    true ->when(getPlanType()){
                        TreatmentPlanType.ELITE,TreatmentPlanType.VIEW -> R.string.account_in_recovery_label to getViewTreatmentPlanDescription(
                            applyNowState)
                        else -> R.string.payment_overdue_label to getString(
                            stringId = R.string.payment_overdue_error_desc,
                            amountOverdue
                        )
                    }
                    false -> R.string.payment_overdue_label to getString(
                        stringId = R.string.payment_overdue_error_desc,
                        amountOverdue
                    )
                }
            }
        }
    }

    override fun getViewTreatmentPlanDescription(applyNowState: ApplyNowState?): String {
        val paymentDueDate = account?.paymentDueDate
        return when (paymentDueDate.isNullOrEmpty()) {
            true ->
               bindString(
                    when (applyNowState) {
                        ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_payment_due_unavailable_desc
                        ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_payment_due_unavailable_desc
                        else -> R.string.account_in_recovery_cc_payment_due_unavailable_desc
                    }
                )

            false -> bindString(
                when (applyNowState) {
                    ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_desc
                    ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_desc
                    else -> R.string.account_in_recovery_cc_desc
                },
                DateFormatter.formatDateTOddMMMYYYY(paymentDueDate, toPattern = "dd MMMM yyyy") ?: ""
            )
        }
    }

    override fun getString(@StringRes stringId: Int, value: String): String =
        bindString(stringId, value)

    override fun isViewPaymentOptionsButtonVisible() =
        if (isViewTreatmentPlan() && isCreditCardProduct()) VISIBLE else GONE

    override fun isMakePaymentButtonVisible(): Int =
        if (isViewTreatmentPlan() && !isCreditCardProduct()) VISIBLE else GONE

    override fun isTreatmentPlanTakeUp() = getPlanType() == TreatmentPlanType.TAKE_UP

    override fun makePaymentPlanButtonLabel() = if (isTreatmentPlanTakeUp())
        bindString(R.string.make_payment_now_button_label)
    else bindString(R.string.view_payment_plan_button_label)

    override fun cannotAffordPaymentFirebaseEvent(): Pair<String, String> {
       return when (applyNowState) {
            ApplyNowState.STORE_CARD ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_SC_ACTION
            ApplyNowState.PERSONAL_LOAN ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_PL_ACTION
            else ->
                FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC to
                        FirebaseManagerAnalyticsProperties.TAKE_UP_TREATMENT_PLAN_CC_ACTION
        }
    }

    override fun isCannotAffordPaymentButtonVisible(): Int  = if (isTreatmentPlanTakeUp()) VISIBLE else GONE

    override fun isAccountChargedOff(): Boolean  = productOfferingStatus.isChargedOff()

    override fun isViewElitePlanEnabled(eligibilityPlan: EligibilityPlan?): Boolean {
        return eligibilityPlan?.actionText.equals(ActionText.VIEW_ELITE_PLAN.value, ignoreCase = true)
    }

    override fun isElitePlanEnabled(eligibilityPlan: EligibilityPlan?): Boolean {
        return eligibilityPlan?.planType.equals(AccountSignedInPresenterImpl.ELITE_PLAN, ignoreCase = true)
    }

    override fun getPopupData(eligibilityPlan: EligibilityPlan?) : DialogData {

        val isCreditCardProduct = isCreditCardProduct()

        val isCollectionTypeViewPlan = when (getPlanType()) {
            TreatmentPlanType.VIEW, TreatmentPlanType.ELITE -> true
            else -> false
        }

        val isViewVipOrElitePlanSupported = (productOfferingStatus.isViewTreatmentPlanSupported()
                || productOfferingStatus.isTakeUpTreatmentPlanJourneyEnabled())

        val amountOverdue = Utils.removeNegativeSymbol(
            CurrencyFormatter.formatAmountToRandAndCent(
                account?.amountOverdue ?: 0
            )
        )

        val descId = getPlanDescription(applyNowState)

        var paymentDueDate = account?.paymentDueDate
        if (!paymentDueDate.isNullOrEmpty())
            paymentDueDate = DateFormatter.formatDateTOddMMMYYYY(paymentDueDate, toPattern = "dd MMMM yyyy")

       return when (isAccountChargedOff()) {
            true -> when (isCreditCardProduct && isViewVipOrElitePlanSupported) {
                true -> when(isCollectionTypeViewPlan){
                    true ->  AccountInDelinquency.AccountInRecovery( desc = descId, formattedValue = paymentDueDate)
                    false -> AccountInDelinquency.TakePlan()
                }
                false -> AccountInDelinquency.ChargedOff()
            }

            false -> when (isViewVipOrElitePlanSupported) {
                    true -> when(isCollectionTypeViewPlan){
                        true -> AccountInArrears.AccountInRecovery(desc = descId, formattedValue = paymentDueDate)
                        false -> AccountInArrears.TakePlan(formattedValue = amountOverdue)
                    }
                    false -> AccountInArrears.InArrears(formattedValue = amountOverdue)
            }
        }
    }

    private fun getAmountOverdue() =
        Utils.removeNegativeSymbol(
            CurrencyFormatter.formatAmountToRandAndCent(
                account?.amountOverdue ?: 0
            )
        )

    override fun getPlanDescription(applyNowState: ApplyNowState?): Int {
        val paymentDueDate = account?.paymentDueDate
        return when (paymentDueDate.isNullOrEmpty()) {
            true ->
                    when (applyNowState) {
                        ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_payment_due_unavailable_desc
                        ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_payment_due_unavailable_desc
                        else -> R.string.account_in_recovery_cc_payment_due_unavailable_desc
                    }


            false -> when (applyNowState) {
                    ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_desc
                    ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_desc
                    else -> R.string.account_in_recovery_cc_desc
                }
        }
    }

}