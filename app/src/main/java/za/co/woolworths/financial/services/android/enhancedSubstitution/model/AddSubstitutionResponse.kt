package za.co.woolworths.financial.services.android.enhancedSubstitution.model

data class AddSubstitutionResponse(
    var `data`: List<DataX>,
    val httpCode: Int,
    val response: Response
)