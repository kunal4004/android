package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import javax.inject.Inject
import kotlin.collections.LinkedHashSet

interface IRetryNetworkRequest {
    val list : LinkedHashSet<NoConnectionService>
    fun put(type : NoConnectionService)
    fun pop(type: NoConnectionService)
    fun isNoConnectionException(type: NoConnectionService): Boolean

    // Get store card request
    fun putStoreCardRequest()
    fun popStoreCardRequest()
    fun isConnectionAvailableForGetStoreCard(): Boolean

    //Get offer active request

    fun putOfferActiveRequest()
    fun popOfferActiveRequest()
    fun isConnectionAvailableForOfferActive(): Boolean

}

enum class NoConnectionService {
    GET_STORE_CARD_REQUEST,
    GET_OFFER_ACTIVE_REQUEST
}

class RetryNetworkRequest @Inject constructor() : IRetryNetworkRequest {

    override val list: LinkedHashSet<NoConnectionService> = linkedSetOf()

    override fun put(type: NoConnectionService) {
        list.add(type)
    }

    override fun pop(type: NoConnectionService) {
        if (list.size > 0)
            list.remove(type)
    }

    override fun isNoConnectionException(type: NoConnectionService): Boolean = list.contains(type)

    override fun putStoreCardRequest()  = put(NoConnectionService.GET_STORE_CARD_REQUEST)

    override fun popStoreCardRequest()  = pop(NoConnectionService.GET_STORE_CARD_REQUEST)

    override fun isConnectionAvailableForGetStoreCard(): Boolean  = isNoConnectionException(NoConnectionService.GET_STORE_CARD_REQUEST)

    override fun putOfferActiveRequest()  = put(NoConnectionService.GET_OFFER_ACTIVE_REQUEST)

    override fun popOfferActiveRequest() = pop(NoConnectionService.GET_OFFER_ACTIVE_REQUEST)

    override fun isConnectionAvailableForOfferActive(): Boolean  = isNoConnectionException(NoConnectionService.GET_OFFER_ACTIVE_REQUEST)

}