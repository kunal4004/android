package za.co.woolworths.financial.services.android.models.dto

data class BPIOverview(
        var overviewTitle: String? = "", var overviewDescription: String? = "",
        var overviewDrawable: Int?, var benfitDescription: Array<String>?,
        var insuranceType: InsuranceType? = null, var benefitHeaderDrawable: Int?)