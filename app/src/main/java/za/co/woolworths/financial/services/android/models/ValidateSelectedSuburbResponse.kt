package za.co.woolworths.financial.services.android.models

import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.ValidatedSuburbProducts

class ValidateSelectedSuburbResponse {
    var validatedSuburbProducts: ValidatedSuburbProducts? = null
    var httpCode: Int = 0
    var response: Response? = null
}