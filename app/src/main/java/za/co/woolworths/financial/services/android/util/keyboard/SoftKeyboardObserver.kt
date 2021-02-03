package za.co.woolworths.financial.services.android.util.keyboard

import android.app.Activity

class SoftKeyboardObserver(activity: Activity) : BaseSoftKeyboardObserver(activity) {

    fun listen(action: (Boolean) -> Unit) {
        internalListen(object : OnKeyboardListener {
            override fun onKeyboardChange(isShow: Boolean) {
                action(isShow)
            }
        })
    }
}