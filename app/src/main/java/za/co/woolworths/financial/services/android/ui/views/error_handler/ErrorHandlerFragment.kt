package za.co.woolworths.financial.services.android.ui.views.error_handler

import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AbsaErrorFragmentLayoutBinding
import za.co.woolworths.financial.services.android.util.LocalConstant
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType

class ErrorHandlerFragment : BaseFragmentBinding<AbsaErrorFragmentLayoutBinding>(AbsaErrorFragmentLayoutBinding::inflate), View.OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        descriptionBlock()
    }

    private fun descriptionBlock() {
        val descBlock1Text = WSpannableStringBuilder(getString(R.string.absa_mobile_app_pin_blocked_desc_block_1))


        val emailLabel = LocalConstant.emailLabel
        val phoneLabel = LocalConstant.phoneLabel
        val textLabel = LocalConstant.textLabel
        val appPinLabel = LocalConstant.appPinLabel

        with(descBlock1Text) {
            makeStringInteractable(emailLabel, LinkType.EMAIL)
            makeStringInteractable(phoneLabel, LinkType.PHONE)
            makeChangeToStringFont(textLabel, R.font.opensans_semi_bold_ttf)
            makeChangeToStringFont(phoneLabel, R.font.opensans_semi_bold_ttf)
            makeChangeToStringFont(emailLabel, R.font.opensans_semi_bold_ttf)
            makeTextFontColor(emailLabel)
            makeTextFontColor(phoneLabel)
            makeTextFontColor(textLabel)
        }

        setTextWithSpanBuilder(binding.errorDescriptionBlock1TextView, descBlock1Text)

        val descBlock2Text = WSpannableStringBuilder(getString(R.string.absa_mobile_app_pin_blocked_desc_block_2))

        with(descBlock2Text) {
            makeStringUnderlined(appPinLabel)
            makeTextFontColor(appPinLabel)
        }
        setTextWithSpanBuilder(binding.errorDescriptionBlock2TextView, descBlock2Text)
    }

    private fun setTextWithSpanBuilder(textView: TextView?, spannableBuilder: WSpannableStringBuilder) {
        textView ?.apply {
            text = spannableBuilder.build()
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
        }
    }

    private fun initListener() {
        binding.actionButton?.apply {
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