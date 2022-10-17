package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.availablefunds

import android.graphics.Color
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType
import javax.inject.Inject

interface IRemoveBlockOnCollection {
    fun getCallCenterContact(phoneNumber : String): SpannableString?
    fun setUnderlineText(spannable: SpannableString?, textView: TextView)
}

class RemoveBlockOnCollectionImpl @Inject constructor(): IRemoveBlockOnCollection {

    override fun getCallCenterContact(phoneNumber: String): SpannableString? {
        val contactCallCenterString = WSpannableStringBuilder(bindString(R.string.contact_the_call_centre_now))
        contactCallCenterString.makeStringInteractable(phoneNumber, LinkType.PHONE)
        contactCallCenterString.makeStringUnderlined(phoneNumber)
        return contactCallCenterString.build()
    }

    override fun setUnderlineText(spannable: SpannableString?, textView: TextView) {
        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }



}