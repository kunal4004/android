package za.co.woolworths.financial.services.android.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommonConnectivityStatusImpl @Inject constructor(
    @ApplicationContext context: Context,
) : CommonConnectivityStatus {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @RequiresApi(Build.VERSION_CODES.N)
    override fun connectivityObserve(): Flow<CommonConnectivityStatus.ConnectivityStatus> {

        return callbackFlow {
            launch { send(CommonConnectivityStatus.Unavailable) }
            val callBack = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(CommonConnectivityStatus.Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { send(CommonConnectivityStatus.Losing) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(CommonConnectivityStatus.Lost) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { send(CommonConnectivityStatus.Unavailable) }
                }
            }
            connectivityManager.registerDefaultNetworkCallback(callBack)
            awaitClose {
                connectivityManager.unregisterNetworkCallback(callBack)
            }

        }.distinctUntilChanged()
    }

}