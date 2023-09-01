package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import androidx.compose.ui.text.AnnotatedString
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.BnplConfig
import za.co.woolworths.financial.services.android.models.dto.app_config.WfsPaymentMethods
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.installmentAmountTag
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.convertTextToAnnotationString
import javax.inject.Inject

interface IManageBnpLConfig {
 fun getComponentTitle(): String?
 fun getComponentDescription(): String?
 fun getInfoLabelAvailableBalance(): String?
 fun getInfoLabelEarnCashback(): String?
 fun getWfsPaymentMethods() : MutableList<WfsPaymentMethods>?
 fun getWfsPaymentMethodsWithPayflex() : MutableList<WfsPaymentMethods>?
 fun isPayFlexBNPLConfigEnabled() : Boolean
 fun installmentCountValue() : Int?
 fun formatPayFlexDescriptionWithPrice(price: String?) : AnnotatedString?
}

class ManageBnplConfigImpl @Inject constructor() : IManageBnpLConfig {

 private val bnplConfig: BnplConfig? by lazy { AppConfigSingleton.productDetailsPage?.bnpl }

 private val payFlex: WfsPaymentMethods? by lazy { bnplConfig?.payflex }
 override fun getComponentTitle(): String?  = bnplConfig?.componentTitle
 override fun getComponentDescription(): String?  = bnplConfig?.componentDescription
 override fun getInfoLabelAvailableBalance(): String?  = bnplConfig?.infoLabelAvailableBalance
 override fun getInfoLabelEarnCashback(): String?  = bnplConfig?.infoLabelEarnCashback
 override fun getWfsPaymentMethods(): MutableList<WfsPaymentMethods>? = bnplConfig?.wfsPaymentMethods

 /**
  * Combines the list of WFS payment methods with the PayFlex payment method,
  * ensuring that PayFlex is added to the list if it's not already present.
  * @return A mutable list containing the combined payment methods.
  */
 override fun getWfsPaymentMethodsWithPayflex(): MutableList<WfsPaymentMethods> {
  return getWfsPaymentMethods()?.apply {
   // Use apply to modify the list and add PayFlex if not already present
   payFlex?.takeIf { payFlex -> payFlex !in this }?.let { payFlex -> add(payFlex) }
  } ?: mutableListOf()   // If WFS payment methods are null, return an empty list
 }

 /**
  * Checks whether the PayFlex Buy Now Pay Later (BNPL) configuration is enabled.
  * This function returns true if both the BNPL feature is required in the current version
  * and the BNPL feature is enabled.
  * @return true if BNPL configuration is enabled, false otherwise.
  */
 override fun isPayFlexBNPLConfigEnabled(): Boolean {
  val isBnplRequired = bnplConfig?.isBnplRequiredInThisVersion ?: false
  val isBnplEnabled = bnplConfig?.isBnplEnabled ?: false
  return isBnplRequired && isBnplEnabled
 }


 /**
  * Retrieves the installment count value from the PayFlex configuration.
  * @return The installment count value if available, or null if not configured.
  */
 override fun installmentCountValue(): Int? {
  return payFlex?.instalmentCount
 }

 /**
  * Formats the PayFlex description with the provided price and returns an AnnotatedString.
  * @param price The price to be inserted into the description.
  * @return An AnnotatedString with the formatted description.
  */
 override fun formatPayFlexDescriptionWithPrice(price: String?): AnnotatedString {
  val standalone = payFlex?.standalone

  // Replace the placeholder in the description with the provided price
  val description = standalone?.description?.replace(installmentAmountTag, price.orEmpty())

  // Replace placeholders in the bold parts of the description with the provided price
  val descriptionBoldParts = standalone?.descriptionBoldParts?.map {
    part -> part.replace(installmentAmountTag, price.orEmpty()) }

  return convertTextToAnnotationString(
   sentence = description,
   wordToFormatList = descriptionBoldParts?.toMutableList(),
   descriptionHyperlinkPart = standalone?.descriptionHyperlinkPart)
 }

}