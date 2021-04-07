package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.MaritalStatus

interface MaritalStatusListener {
    fun setMaritalStatus(maritalStatus: MaritalStatus)
}
