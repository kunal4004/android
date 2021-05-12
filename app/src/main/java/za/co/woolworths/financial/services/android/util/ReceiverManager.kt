package za.co.woolworths.financial.services.android.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*

class ReceiverManager private constructor(context: Context) {
    private val cReference: WeakReference<Context> = WeakReference(context)
    fun registerReceiver(receiver: BroadcastReceiver, intentFilter: IntentFilter): Intent? {
        receivers.add(receiver)
        val intent = cReference.get()?.registerReceiver(receiver, intentFilter)
        Log.i(javaClass.simpleName, "registered receiver: $receiver  with filter: $intentFilter")
        Log.i(javaClass.simpleName, "receiver Intent: $intent")
        return intent
    }

    fun isReceiverRegistered(receiver: BroadcastReceiver): Boolean {
        val registered = receivers.contains(receiver)
        Log.i(javaClass.simpleName, "is receiver $receiver registered? $registered")
        return registered
    }

    fun unregisterReceiver(receiver: BroadcastReceiver) {
        if (isReceiverRegistered(receiver)) {
            receivers.remove(receiver)
            try {
                cReference.get()?.unregisterReceiver(receiver)
                Log.i(javaClass.simpleName, "unregistered receiver: $receiver")
            } catch (ex: IllegalAccessException) {
                FirebaseManager.logException(ex)
            }
        }
    }

    companion object {
        private val receivers: MutableList<BroadcastReceiver> = ArrayList()
        private var ref: ReceiverManager? = null

        @Synchronized
        fun init(context: Context): ReceiverManager? {
            if (ref == null) ref = ReceiverManager(context)
            return ref
        }
    }

}