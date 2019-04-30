package za.co.woolworths.financial.services.android.util

import android.text.method.PasswordTransformationMethod
import android.view.View

class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        return PasswordCharSequence(source!!)
    }

    private class PasswordCharSequence(source: CharSequence) : CharSequence {
        var mSource: CharSequence = source

        override fun get(index: Int): Char {
            val string = "*"
            return string.single()
        }

        override val length: Int
            get() = mSource.length

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return mSource.subSequence(startIndex, endIndex)
        }
    }
}