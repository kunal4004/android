package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.annotation.StringRes
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.ActionText
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlan
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.treatmentplan.ProductOfferingStatus
import za.co.woolworths.financial.services.android.ui.extension.bindString
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
    fun getString(@StringRes stringId: Int, value: String): String
    fun isViewPaymentOptionsButtonVisible(): Int
    fun isMakePaymentButtonVisible(): Int
    fun isTreatmentPlanTakeUp(): Boolean
    fun makePaymentPlanButtonLabel(): String
    fun cannotAffordPaymentFirebaseEvent(): Pair<String, String>
    fun isCannotAffordPaymentButtonVisible(): Int
    fun isProductChargedOff() : Boolean
}

enum class TreatmentPlanType {
    VIEW, TAKE_UP, ELITE, NONE
}

class ViewTreatmentPlanImpl(
    private val context: Context?,
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
        val isCreditCardProduct = isCreditCardProduct()
        val amountOverdue = Utils.removeNegativeSymbol(
            CurrencyFormatter.formatAmountToRandAndCent(
                account?.amountOverdue ?: 0
            )
        )
        return when (isProductChargedOff()) {
            true -> {
                when (isCreditCardProduct && (productOfferingStatus.isViewTreatmentPlanSupported()
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

    override fun getViewTreatmentPlanDescription(applyNowState: ApplyNowState?): String? {
        val paymentDueDate = account?.paymentDueDate
        return when (paymentDueDate.isNullOrEmpty()) {
            true ->
                context?.resources?.getString(
                    when (applyNowState) {
                        ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_payment_due_unavailable_desc
                        ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_payment_due_unavailable_desc
                        else -> R.string.account_in_recovery_cc_payment_due_unavailable_desc
                    }
                )

            false -> context?.resources?.getString(
                when (applyNowState) {
                    ApplyNowState.PERSONAL_LOAN -> R.string.account_in_recovery_pl_desc
                    ApplyNowState.STORE_CARD -> R.string.account_in_recovery_sc_desc
                    else -> R.string.account_in_recovery_cc_desc
                },
                DateFormatter.formatDateTOddMMMYYYY(paymentDueDate, toPattern = "dd MMMM yyyy")
            )
        }
    }

    override fun getString(@StringRes stringId: Int, value: String): String =
        context?.getString(stringId, value) ?: ""

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

    override fun isProductChargedOff(): Boolean  = productOfferingStatus.isChargedOff()

}