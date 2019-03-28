package za.co.woolworths.financial.services.android.util

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

abstract class ConnectionBroadcastReceiver : BroadcastReceiver() {
    companion object {
        @JvmStatic
        fun registerWithoutAutoUnregister(context: Context, connectionBroadcastReceiver: ConnectionBroadcastReceiver) {
            context.registerReceiver(connectionBroadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        }

        @JvmStatic
        fun registerToFragmentAndAutoUnregister(context: Context, fragment: Fragment, connectionBroadcastReceiver: ConnectionBroadcastReceiver) {
            val applicationContext = context.applicationContext
            registerWithoutAutoUnregister(applicationContext, connectionBroadcastReceiver)
            fragment.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    applicationContext.unregisterReceiver(connectionBroadcastReceiver)
                }
            })
        }

        @JvmStatic
        fun registerToActivityAndAutoUnregister(activity: AppCompatActivity, connectionBroadcastReceiver: ConnectionBroadcastReceiver) {
            registerWithoutAutoUnregister(activity, connectionBroadcastReceiver)
            activity.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    activity.unregisterReceiver(connectionBroadcastReceiver)
                }
            })
        }

        @JvmStatic
        fun hasInternetConnection(context: Context): Boolean {
            val info = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
            return !(info == null || !info.isConnectedOrConnecting)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val hasConnection = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        onConnectionChanged(hasConnection)
    }

    abstract fun onConnectionChanged(hasConnection: Boolean)
}