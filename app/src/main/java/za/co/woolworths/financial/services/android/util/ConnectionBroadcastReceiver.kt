package za.co.woolworths.financial.services.android.util


import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

abstract class ConnectionBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"

        @JvmStatic
        fun registerWithoutAutoUnregister(
            context: Context,
            connectionBroadcastReceiver: ConnectionBroadcastReceiver
        ) {
            context.registerReceiver(connectionBroadcastReceiver, IntentFilter(CONNECTIVITY_ACTION))
        }

        @JvmStatic
        fun registerToServiceAndAutoUnregister(
             context: Context,
             connectionBroadcastReceiver: ConnectionBroadcastReceiver
        ) {
            context.registerReceiver(connectionBroadcastReceiver, IntentFilter(CONNECTIVITY_ACTION))
        }

        @JvmStatic
        fun registerToFragmentAndAutoUnregister(
            context: Context,
            fragment: Fragment,
            connectionBroadcastReceiver: ConnectionBroadcastReceiver
        ) {
            val applicationContext = context.applicationContext
            applicationContext ?: return
            registerWithoutAutoUnregister(applicationContext, connectionBroadcastReceiver)
            fragment.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    applicationContext.unregisterReceiver(connectionBroadcastReceiver)
                }
            })
        }

        @JvmStatic
        fun registerToActivityAndAutoUnregister(
            activity: AppCompatActivity?,
            connectionBroadcastReceiver: ConnectionBroadcastReceiver
        ) {
            activity?.let { registerWithoutAutoUnregister(it, connectionBroadcastReceiver) }
            activity?.lifecycle?.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    activity.unregisterReceiver(connectionBroadcastReceiver)
                }
            })
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val hasConnection =
            !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        onConnectionChanged(hasConnection)
    }

    abstract fun onConnectionChanged(hasConnection: Boolean)
}