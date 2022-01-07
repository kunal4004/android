package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigMaritalStatus

interface MaritalStatusListener {
    fun setMaritalStatus(maritalStatus: ConfigMaritalStatus)
    fun getMaritalStatus(): ConfigMaritalStatus
}
