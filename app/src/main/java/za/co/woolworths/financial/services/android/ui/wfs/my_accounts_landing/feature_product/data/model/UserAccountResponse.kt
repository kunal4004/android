package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import za.co.woolworths.financial.services.android.models.dto.InsuranceType
import za.co.woolworths.financial.services.android.models.dto.account.BpiInsuranceApplicationStatusType
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse

@Parcelize
data class UserAccountResponse(
    var httpCode: Int = 0,
    var account: ProductDetails? = null,
    var accountList: MutableList<ProductDetails>? = null,
    var products: MutableList<ProductDetails>? = null,
    var response: ServerErrorResponse? = null
) : Parcelable

@Parcelize
data class ProductDetails(
    var productOfferingId: Int,
    var creditLimit: Int? = 0,
    var currentBalance: Int,
    var availableFunds: Int? = null,
    var minimumAmountDue: Int? = null,
    var paymentDueDate: String? = "",
    var minDrawDownAmount: Int? = 0,
    var rpCreditLimitThreshold: Int? = null,
    var productGroupCode: String,
    var accountNumberBin: String? = null,
    var productOfferingStatus: String?,
    var productOfferingGoodStanding: Boolean? = false,
    var totalAmountDue: Int? = null,
    var amountOverdue: Int? = null,
    var paymentMethods: MutableList<PaymentMethod>? = null,
    var delinquencyCycle: Int? = null,
    var bankingDetails: @RawValue JsonElement? = null,
    var debitOrder: DebitOrder? = null,
    var insuranceCovered: Boolean? = null,
    var bpiInsuranceApplication: BpiInsuranceApplication? = null,
    var insuranceTypes: MutableList<InsuranceType>? = null,
    var cards: MutableList<ABSACard>? = null,
    var accountNumber: String? = null,
    var primaryCard: PrimaryCard? = null,
    var isAccountChargedOff : Boolean? = false, //local parameters
    var availableFundsFormatted : String?,
    var isRetryButtonEnabled : Boolean = false,
    var isRetryInProgress : Boolean? =false
) : Parcelable

@Parcelize
data class PaymentMethod(val description: String) : Parcelable

@Parcelize
data class DebitOrder(
    val debitOrderActive: Boolean? = false,
    var debitOrderDeductionDay: String? = null,
    var debitOrderProjectedAmount: Float = 0f
) : Parcelable

@Parcelize
data class BpiInsuranceApplication(
    val status: BpiInsuranceApplicationStatusType?, // can be converted to enum
    val displayLabel: String?,
    val displayLabelColor: String?
) : Parcelable

@Parcelize
data class ABSACard(
    var productCategory: String? = null,
    var productStatus: String? = null,
    var cardStatus: String? = null,
    var absaCardToken: String? = null,
    var absaAccountToken: String? = null,
    var envelopeNumber: String? = null
) : Parcelable

@Parcelize
data class PrimaryCard(
    var cardBlocked: Boolean? = null,
    var cards: MutableList<ABSACard>? = null
) : Parcelable
