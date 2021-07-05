package za.co.woolworths.financial.services.android.util

import android.app.Application
import android.content.Context
import android.net.*
import androidx.lifecycle.LiveData

object ConnectivityLiveData : LiveData<Boolean>() {

    private lateinit var application: Application
    private lateinit var networkRequest: NetworkRequest
    private var connectivityService: ConnectivityManager? = null

    fun init(application: Application) {
        this.application = application
        this.networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(true)
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(false)
        }
    }

    override fun onActive() {
        super.onActive()
        connectivityService = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityService?.registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        connectivityService?.unregisterNetworkCallback(networkCallback)
    }

    fun isNetworkAvailable(): Boolean {
        val cm = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }
}