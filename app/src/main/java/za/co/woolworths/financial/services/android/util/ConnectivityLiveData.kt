package za.co.woolworths.financial.services.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

class ConnectivityLiveData @Inject constructor(context: Context) : LiveData<Boolean>() {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager
    private lateinit var networkRequestBuilder : ConnectivityManager.NetworkCallback
    
    override fun onActive() {
        super.onActive()
        networkRequestBuilder = getNetworkCallback()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(networkRequestBuilder)
        } else {
            connectivityManager?.registerNetworkCallback(getNetworkRequest(), networkRequestBuilder)
        }
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager?.unregisterNetworkCallback(networkRequestBuilder)
        } catch (e: Exception) {
            FirebaseManager.logException(e)
        }
    }

    private fun getNetworkRequest(): NetworkRequest {
        val networkRequestBuilder = NetworkRequest.Builder()
        with(networkRequestBuilder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } else {
                addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            }
        }
        return networkRequestBuilder.build()
    }

    private fun getNetworkCallback() = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

}