package za.co.woolworths.financial.services.android.models.dto.linkdevice

import za.co.woolworths.financial.services.android.models.dto.Response
import java.io.Serializable

class ViewAllLinkedDeviceResponse(var userDevices: ArrayList<UserDevice>?, var response: Response?, var httpCode: Int?) : Serializable