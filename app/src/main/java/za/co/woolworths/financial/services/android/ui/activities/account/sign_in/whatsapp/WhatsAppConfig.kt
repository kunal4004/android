package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.whatsapp.WhatsApp

class WhatsAppConfig {

    private var whatsAppConfig: WhatsApp? = null

    init {
        whatsAppConfig = WoolworthsApplication.getWhatsAppConfig()
    }

    val whatsAppNumber: String
        get() = whatsAppConfig?.phoneNumber?.replace("..(?!$)", "$0 ") ?: ""


    val ccPaymentOptionsIsEnabled: Boolean
        get() = whatsAppConfig?.apply { showWhatsAppButton && showWhatsAppIcon.ccPaymentOptions }?.let { false }
                ?: false


    val contactUsFinancialServicesIsEnabled: Boolean
        get() {
          return whatsAppConfig?.showWhatsAppButton!! && whatsAppConfig?.showWhatsAppIcon?.contactUsFinancialServices!!
        }


    val whatsAppChatWithUsUrlBreakout: String?
        get() = "${whatsAppConfig?.baseUrl}${whatsAppConfig?.phoneNumber}${whatsAppConfig?.text} "
}