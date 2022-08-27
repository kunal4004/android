package za.co.woolworths.financial.services.android.common

import kotlinx.coroutines.flow.Flow

interface CommonConnectivityStatus {

    fun connectivityObserve(): Flow<ConnectivityStatus>

    sealed class ConnectivityStatus
    object Available : ConnectivityStatus()
    object Unavailable : ConnectivityStatus()
    object Losing : ConnectivityStatus()
    object Lost : ConnectivityStatus()

}