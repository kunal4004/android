package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import javax.inject.Inject
interface IManageShopOptimiserCalculation {
    fun payFlexCalculation(fbhProductPrice: Double?): String
}

class ManageShopOptimiserCalculationImpl @Inject constructor(manageBnplConfig : IManageBnpLConfig) : IManageShopOptimiserCalculation,
    IManageBnpLConfig by manageBnplConfig {

    /**
     * Calculates the PayFlex installment amount for a given product price.
     * @param fbhProductPrice The price of the product.
     * @return A formatted string representing the calculated installment amount.
     */
    override fun payFlexCalculation(fbhProductPrice: Double?): String {
        val instalmentCount = installmentCountValue()
        val installmentAmount: Double? = instalmentCount?.let { installment ->
            fbhProductPrice?.div(installment)
        }
        return CurrencyFormatter.formatAmountToRandAndCentNoSpace(installmentAmount)
    }

}