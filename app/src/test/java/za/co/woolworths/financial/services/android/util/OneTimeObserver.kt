package za.co.woolworths.financial.services.android.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer

/**
 * Created by Kunal Uttarwar on 23/11/21.
 */
class OneTimeObserver<T>(private val handler: (T) -> Unit, override val lifecycle: Lifecycle) : Observer<T>, LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }


    override fun onChanged(t: T) {
        handler(t)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}