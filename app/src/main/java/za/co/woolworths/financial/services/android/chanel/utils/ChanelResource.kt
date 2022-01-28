package za.co.woolworths.financial.services.android.chanel.utils

import za.co.woolworths.financial.services.android.chanel.model.ChanelResponse
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

class ChanelResource (
    val responseStatus: ResponseStatus,
    val data: ChanelResponse?,
    val message: String?) {

    companion object {

        fun success(data: ChanelResponse): ChanelResource {
            return ChanelResource(ResponseStatus.SUCCESS, data, null)
        }

        fun  error(msg: String, data: ChanelResponse?): ChanelResource {
            return ChanelResource(ResponseStatus.ERROR, data, msg)
        }

        fun loading(data: ChanelResponse?): ChanelResource {
            return ChanelResource(ResponseStatus.LOADING, data, null)
        }
    }
}