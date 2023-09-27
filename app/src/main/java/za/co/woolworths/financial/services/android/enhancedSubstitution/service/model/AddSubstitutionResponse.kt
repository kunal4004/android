package za.co.woolworths.financial.services.android.enhancedSubstitution.service.model

data class AddSubstitutionResponse(
    var data: List<DataX>,
    val httpCode: Int,
    val response: Response
)