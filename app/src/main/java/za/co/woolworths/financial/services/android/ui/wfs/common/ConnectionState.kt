package za.co.woolworths.financial.services.android.ui.wfs.common

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Available : ConnectionState()
    object Unavailable : ConnectionState()
}