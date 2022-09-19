package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.widget.TextView
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delete_account_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment


class DeleteAccountBottomSheetDialog : WBottomSheetDialogFragment() ,
    View.OnClickListener{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.delete_account_bottom_sheet_dialog,
            container,
            false
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        actionButton?.paint?.isUnderlineText = true
        initClick()
    }

    private fun initClick() {
        cancelButton?.setOnClickListener(this)
        actionButton?.setOnClickListener(this)
        setMessageWithClickableLink(deleteDescription_2)
    }
    private fun setMessageWithClickableLink(textView: TextView?) {
        val content =
            getString(R.string.to_contact_customer_support_dial_0860_100_987) + " " + getString(R.string.online_local_caller_number)
        val phone = getString(R.string.online_local_caller_number)
        //Clickable Span will help us to make clickable a text
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                //code for calling
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:+$phone")
                startActivity(intent)
            }

        }
        val startIndex = content.indexOf(phone)
        val endIndex = startIndex + phone.length
        //SpannableString will be created with the full content and
        // the clickable content all together
        val spannableString = SpannableString(content)
        //only the phone is clickable
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        //The following is to set the new text in the TextView
        //no styles for an already clicked link
        textView?.text = spannableString
        textView?.movementMethod = LinkMovementMethod.getInstance()
        textView?.highlightColor = Color.BLUE
    }


    companion object {
        const val DELETE_ACCOUNT = "DELETE_ACCOUNT"
        const val DELETE_ACCOUNT_CONFIRMATION = "DELETE_ACCOUNT_CONFIRMATION"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> {
                setFragmentResult(DELETE_ACCOUNT_CONFIRMATION, bundleOf(DELETE_ACCOUNT to DELETE_ACCOUNT))
                dismiss()
            }
            R.id.cancelButton -> {
                dismiss()
            }
        }
    }


}