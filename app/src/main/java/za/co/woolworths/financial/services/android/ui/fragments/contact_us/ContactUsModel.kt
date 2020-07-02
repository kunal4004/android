package za.co.woolworths.financial.services.android.ui.fragments.contact_us

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.contact_us.*

class ContactUsModel {

    private var contactUsList: MutableList<ContactUs>? = null

    init {
        contactUsList = WoolworthsApplication.getContactUs()
    }

    fun contactUsLanding(): MutableList<ContactUs>? {
        return contactUsList
    }


    fun contactUsFinancialService(): ContactUs? {
        return contactUsList?.get(0)
    }

    private fun contactUsFinancialServiceOptions(): ContactUsValue? {
        return contactUsFinancialService()?.options?.get(0)?.value
    }

    fun contactUsFinancialServicesCall(): ContactUsCall? {
        return contactUsFinancialServiceOptions()?.call
    }

    fun contactUsFinancialServicesEmail(): List<Options>? {
        return contactUsFinancialServiceOptions()?.email
    }

    fun contactUsCustomerServices(): ContactUs? {
        return contactUsList?.get(1)
    }

    fun contactUsCustomerServicesOptions(): ArrayList<ContactUsOptions>? {
        return contactUsList?.get(1)?.options
    }
}