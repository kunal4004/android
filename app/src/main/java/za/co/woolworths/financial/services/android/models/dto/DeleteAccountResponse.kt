package za.co.woolworths.financial.services.android.models.dto


data class DeleteAccountResponse(
    val httpCode: Int,
    val links: List<Any>,
    val message: String,
    val response: Any
)

