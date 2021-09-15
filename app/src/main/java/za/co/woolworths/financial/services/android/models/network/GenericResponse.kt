package za.co.woolworths.financial.services.android.models.network

import za.co.woolworths.financial.services.android.models.dto.Response
import java.io.Serializable

class GenericResponse(var response: Response?, var httpCode: Int?) : Serializable