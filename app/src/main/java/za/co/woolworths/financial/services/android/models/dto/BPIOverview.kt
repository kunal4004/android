package za.co.woolworths.financial.services.android.models.dto

data class BPIOverview(var overviewDescription: String? = "", var overviewDrawable: Int?,
                       var benfitDescription: Array<String>?, var insuranceType: InsuranceType?,
                       var benefitHeaderDrawable: Int?)