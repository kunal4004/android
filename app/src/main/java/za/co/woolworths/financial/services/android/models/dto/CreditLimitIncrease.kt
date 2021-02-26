package za.co.woolworths.financial.services.android.models.dto

import java.io.Serializable

data class CreditLimitIncrease(
        val eligibilityQuestions: EligibilityQuestions? = null,
        val permissions: Permissions? = null,
        var maritalStatusList: ArrayList<MaritalStatus>? = null
) : Serializable {

    fun init(){
        maritalStatusList = ArrayList(0)
        maritalStatusList?.add(MaritalStatus(1, "Single"))
        maritalStatusList?.add(MaritalStatus(2, "Married"))
        maritalStatusList?.add(MaritalStatus(3, "Divorced"))
        maritalStatusList?.add(MaritalStatus(4, "Widow"))
        maritalStatusList?.add(MaritalStatus(5, "Common Law"))
        maritalStatusList?.add(MaritalStatus(6, "Married in Community"))
    }
}