package za.co.woolworths.financial.services.android.enhancedSubstitution.model


data class ProductSubstitution(
        var data: List<Data>,
        val httpCode: Int,
        val response: Response)