package za.co.woolworths.financial.services.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.Status
import za.co.woolworths.financial.services.android.contracts.IOTPReceiveListener


class SMSReceiver : BroadcastReceiver() {

    private var otpListener: IOTPReceiveListener? = null

    fun setOTPListener(otpListener: IOTPReceiveListener) {
        this.otpListener = otpListener
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status = extras!!.get(SmsRetriever.EXTRA_STATUS) as Status
            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    //This is the full message
                    val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                    /*<#> Your ExampleApp code is: 123ABC78
                    FA+9qCX9VSu*/
                    //Extract the OTP code and send to the listener
                    otpListener?.onOTPReceived(message)
                }
                // Waiting for SMS timed out (5 minutes)
                CommonStatusCodes.TIMEOUT -> otpListener?.onOTPTimeOut()

                CommonStatusCodes.API_NOT_CONNECTED -> otpListener?.onOTPReceivedError("API NOT CONNECTED")

                CommonStatusCodes.NETWORK_ERROR -> otpListener?.onOTPReceivedError("NETWORK ERROR")

                CommonStatusCodes.ERROR -> otpListener?.onOTPReceivedError("SOME THING WENT WRONG")
            }
        }
    }
}