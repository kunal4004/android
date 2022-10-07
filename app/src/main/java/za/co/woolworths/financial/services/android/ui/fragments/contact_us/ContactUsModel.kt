package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUs
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUsCall
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUsOptions
import za.co.woolworths.financial.services.android.models.dto.app_config.contact_us.ConfigContactUsValue

class ContactUsModel {

    private var contactUsList: MutableList<ConfigContactUs>? = null

    init {
        contactUsList = AppConfigSingleton.mContactUs
    }

    fun contactUsLanding(): MutableList<ConfigContactUs>? {
        return contactUsList
    }


    fun contactUsFinancialService(): ConfigContactUs? {
        return contactUsList?.get(0)
    }

    private fun contactUsFinancialServiceOptions(): ConfigContactUsValue? {
        return contactUsFinancialService()?.options?.get(0)?.value
    }

    fun contactUsFinancialServicesCall(): ConfigContactUsCall? {
        return contactUsFinancialServiceOptions()?.call
    }

    fun contactUsFinancialServicesEmail(): List<ConfigOptions>? {
        return contactUsFinancialServiceOptions()?.email
    }

    fun contactUsCustomerServices(): ConfigContactUs? {
        return contactUsList?.get(1)
    }

    fun contactUsCustomerServicesOptions(): ArrayList<ConfigContactUsOptions>? {
        return contactUsList?.get(1)?.options
    }

    fun contactUsDashServices(): ConfigContactUs? {
        return contactUsList?.get(2)
    }

    fun contactUsDashServicesOptions(): ArrayList<ConfigContactUsOptions>? {
        return contactUsList?.get(2)?.options
    }
}