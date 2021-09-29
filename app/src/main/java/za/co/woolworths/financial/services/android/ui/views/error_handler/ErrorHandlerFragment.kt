package za.co.woolworths.financial.services.android.ui.views.error_handler

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_error_fragment_layout.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType

class ErrorHandlerFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.absa_error_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        descriptionBlock()
    }

    private fun descriptionBlock() {
        val descBlock1Text = WSpannableStringBuilder(getString(R.string.absa_mobile_app_pin_blocked_desc_block_1))

        val emailLabel = "queries@wfs.co.za"
        val phoneLabel = "086 150 2020"
        val textLabel = "release the hold"

        with(descBlock1Text) {
            makeStringInteractable(emailLabel, LinkType.EMAIL)
            makeStringInteractable(phoneLabel, LinkType.PHONE)
            makeChangeToStringFont(textLabel, R.font.myriad_pro_semi_bold_otf)
            makeChangeToStringFont(phoneLabel, R.font.myriad_pro_semi_bold_otf)
            makeChangeToStringFont(emailLabel, R.font.myriad_pro_semi_bold_otf)
            makeTextFontColor(emailLabel)
            makeTextFontColor(phoneLabel)
            makeTextFontColor(textLabel)
        }

        errorDescriptionBlock1TextView?.apply {
            text = descBlock1Text.build()
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun initListener() {
        actionButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@ErrorHandlerFragment)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> activity?.onBackPressed()
        }
    }
}