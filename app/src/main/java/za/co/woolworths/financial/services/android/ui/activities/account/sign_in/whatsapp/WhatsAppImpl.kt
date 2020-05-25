package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import za.co.woolworths.financial.services.android.util.DateTimeUtils
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.whatsapp.WhatsApp

import java.util.*

class WhatsAppImpl {

    companion object {
        const val CC_PAYMENT_OPTIONS = "Payment Options"
        const val FEATURE_NAME = "FEATURE_NAME"
        const val APP_SCREEN = "APP_SCREEN"
        const val CONTACT_US = "Contact Us"
        const val FEATURE_WHATSAPP = "whatsApp"
    }

    private var whatsAppConfig: WhatsApp? = null

    init {
        whatsAppConfig = WoolworthsApplication.getWhatsAppConfig()
    }

    // Get hour in 24 hour format
    private val hourMinutesIn24HourFormat: Date?
        get() {
            val currentTime = DateTimeUtils().currentDayZoneTime
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute: Int = currentTime.get(Calendar.MINUTE)
            return DateTimeUtils().parseDate("$hour:$minute")
        }

    private val customerServiceAvailableTimeFrom: Date?
        get() = whatsAppConfig?.availabilityTimes?.startTime?.let { DateTimeUtils().parseDate(it) }
                ?: Date(0)

    private val customerServiceAvailableTimeUntil: Date?
        get() = whatsAppConfig?.availabilityTimes?.endTime?.let { DateTimeUtils().parseDate(it) }
                ?: Date(0)

    val whatsAppNumber: String
        get() {
            whatsAppConfig?.phoneNumber?.apply {
                val phoneNumber = "0${substring(2, length)}"
                val formattedPhoneNumber = phoneNumber.replace("...".toRegex(), "$0 ")
                return "${formattedPhoneNumber.substring(0, formattedPhoneNumber.length - 2)}${formattedPhoneNumber.substring(formattedPhoneNumber.length - 2, formattedPhoneNumber.length).trim()}"
            }
            return ""
        }

    val ccPaymentOptionsIsEnabled: Boolean
        get() = whatsAppConfig?.showWhatsAppButton!! && whatsAppConfig?.showWhatsAppIcon?.ccPaymentOptions!!

    val isChatWithUsEnabledForContactUs: Boolean
        get() = whatsAppConfig?.showWhatsAppButton!! && whatsAppConfig?.showWhatsAppIcon?.contactUsFinancialServices!!

    val whatsAppChatWithUsUrlBreakout: String?
        get() = "${whatsAppConfig?.baseUrl}${whatsAppConfig?.phoneNumber}${whatsAppConfig?.text} "

    val isCustomerServiceAvailable: Boolean
        get() = customerServiceAvailableTimeFrom?.before(hourMinutesIn24HourFormat) ?: false && customerServiceAvailableTimeUntil?.after(hourMinutesIn24HourFormat) ?: false

}