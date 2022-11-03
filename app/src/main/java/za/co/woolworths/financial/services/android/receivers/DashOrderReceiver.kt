package za.co.woolworths.financial.services.android.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DashOrderReceiver: BroadcastReceiver() {
    private lateinit var listener: DashOrderReceiverListener

    companion object{
        const val ACTION_LAST_DASH_ORDER = "za.co.woolworths.financial.services.android.receivers.DashOrderReceiver.RECEIVE_ORDER_UPDATE"
        const val EXTRA_UNREAD_MESSAGE_COUNT = "EXTRA_UNREAD_MESSAGE_COUNT"
        const val EXTRA_UPDATE_LAST_DASH_ORDER = "EXTRA_UPDATE_LAST_DASH_ORDER"
    }

    fun setDashOrderReceiverListener(listener: DashOrderReceiverListener) {
        this.listener = listener
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if(intent?.action == ACTION_LAST_DASH_ORDER) {
            intent.extras?.apply {
                when {
                    containsKey(EXTRA_UNREAD_MESSAGE_COUNT) -> {
                        listener.updateUnreadMessageCount(getInt(EXTRA_UNREAD_MESSAGE_COUNT, 0))
                    }
                    containsKey(EXTRA_UPDATE_LAST_DASH_ORDER) -> {
                        if(getBoolean(EXTRA_UPDATE_LAST_DASH_ORDER, false))
                        listener.updateLastDashOrder()
                    }
                }
            }
        }
    }
}