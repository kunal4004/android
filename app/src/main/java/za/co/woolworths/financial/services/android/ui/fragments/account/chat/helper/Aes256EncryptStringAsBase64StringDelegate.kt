package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import za.co.woolworths.financial.services.android.util.Utils
import kotlin.reflect.KProperty

class Aes256EncryptStringAsBase64StringDelegate {

    var encryptedValue: String? = null

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {

        if (value != null) {
            encryptedValue = Utils.aes256EncryptStringAsBase64String(value)
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return encryptedValue
    }
}