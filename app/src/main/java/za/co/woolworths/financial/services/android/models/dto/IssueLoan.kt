package za.co.woolworths.financial.services.android.models.dto

data class IssueLoan(var productOfferingId: Int, var drawDownAmount: Int,
                     var repaymentPeriod: Int, var creditLimit: Int)