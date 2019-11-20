package za.co.woolworths.financial.services.android.contracts

interface IOTPReceiveListener {
    fun onOTPReceived(otp: String)
    fun onOTPTimeOut()
    fun onOTPReceivedError(error: String)
}